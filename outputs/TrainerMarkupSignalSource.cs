using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;

// Pure managed source model. It neither loads native modules nor writes trainer
// memory, files, physical position, protection outputs or route configuration.
// Native clean-state map: Trenager_3D.exe SHA E217148C24427CAE5DAB46309676854C0E73274F79381AE62613D9D63B3C1612,
// TLighterBasis encoder RVA24AF20 /26-entry jump table RVA24AF52. Runtime fault
// overrides and occupancy are not reconstructed from saved course defaults.
public sealed class TrainerMarkupSignalSnapshot {
    public int Id, RecordIndex, Meter, Type, Lens, State, OriginalState;
    public int Availability, Autoblocking;
    public bool SendCode, BadWay, TeachingOverride;
    public string Name, StateName, OriginalStateName, StateOrigin;
    public byte NativeBasicCodeWay;
    public long Generation;
}

public sealed class TrainerMarkupProfileSnapshot {
    public int RecordIndex;
    // Exact native TProfE field values. Uklon is not relabelled as a proved unit.
    public double Nach, Len, Uklon;
}

public sealed class TrainerMarkupStationSnapshot {
    public int Id, Meter, IdVhodLight, IdVyhodLight, IdVhodArrow, IdVyhodArrow, WayTyp;
    public string Name, WayNumber;
}

public sealed class TrainerMarkupPiketSnapshot {
    public int RecordIndex, RecordVersion, KM, Picet, Offset, Direction;
    public string DirectionName;
}

public sealed class TrainerMarkupSourceMetadata {
    public string Path, Sha256, ImportManifestRelativePath, HashStatus;
    public int Bytes, Count, FullConsumedSize;
}

public sealed class TrainerMarkupSourceFrame {
    // Source-only TCurLighterRec fields: Index+00, Name shortstr20+04,
    // Xlight+19, XlastLight+1D, SendCode+21, BlocUch+22.
    // The host owns the separately proved fixed21-byte native Name encoding.
    // No Delphi string allocation or derived Dist/Uklon/Vpr/Vcel is exposed here.
    public int Index, Xlight, XlastLight, State, OriginalState;
    public string Name, StateName, SignalStateOrigin, CodeWayOrigin;
    public bool SendCode, TeachingOverride, PassedStop;
    public byte BlocUch, CodeWay;
    public int PassedStopSignalId = -1, PassedStopState = -1;
    public double NativeX;
    public long Generation;
    public bool HasRawAlsn { get { return false; } }
}

public sealed class TrainerMarkupSignalSource {
    const int MaximumJsonBytes = 8000000;
    static readonly int[] CleanStateCodes = {
        1, 3, 2, 0, 0, 0, 1, 1, 1, 1, 2, 1, 2,
        2, 3, 3, 1, 2, 1, 0, 0, 0, 0, 3, 3, 0
    };
    static readonly string[] CleanStateNames = {
        "SlGreen", "SlRed", "SlYellow", "SlBlue", "SlWhite", "SlBlack",
        "SlBlinkGreen", "SlBlinkGreenAndYellow", "SlBlinkYellow", "SlBlinkYellowAndWhite",
        "SlDoubleYellow", "SlDoubleGreen", "SlTriYellow", "SlDoubleYellowBlink",
        "SlRedAndWhite", "SlRedAndBlinkWhite", "SlYellowAndGreen", "SlYellowAndWhite",
        "SlGreenAndWhite", "SlOffSignal", "SlOnAllSignal", "SlUnknowSignal",
        "SlBlinkWhite", "SlRedAndYellow", "SlBlinkRed", "SlWhiteAndWhite"
    };

    readonly object gate = new object();
    readonly TrainerMarkupSignalSnapshot[] originalSignals;
    readonly TrainerMarkupProfileSnapshot[] profiles;
    readonly TrainerMarkupStationSnapshot[] stations;
    readonly TrainerMarkupPiketSnapshot[] pikets;
    readonly Dictionary<int, int> indexById;
    readonly TrainerMarkupSourceMetadata lightMetadata, profileMetadata, stationMetadata, piketMetadata;
    readonly int[] states;
    readonly bool[] teachingOverrides;
    readonly string sourceName, sourcePath, sourceSha256;
    readonly double nativeCourseStart, nativeCourseEnd, stationKokPrefixEnd;
    readonly int orderedPiketPrefixCount;
    bool hasPosition;
    double lastPosition;
    bool passedStop;
    int blockedIndex = -1, blockedState = -1;
    long generation;

    TrainerMarkupSignalSource(string name, string path, string jsonSha,
        TrainerMarkupSignalSnapshot[] signalRows, TrainerMarkupProfileSnapshot[] profileRows,
        TrainerMarkupStationSnapshot[] stationRows, TrainerMarkupPiketSnapshot[] piketRows,
        TrainerMarkupSourceMetadata lightMeta, TrainerMarkupSourceMetadata profileMeta,
        TrainerMarkupSourceMetadata stationMeta, TrainerMarkupSourceMetadata piketMeta,
        double start, double end, double kokEnd, int orderedPrefix) {
        sourceName = name; sourcePath = path; sourceSha256 = jsonSha;
        originalSignals = signalRows; profiles = profileRows; stations = stationRows; pikets = piketRows;
        lightMetadata = lightMeta; profileMetadata = profileMeta;
        stationMetadata = stationMeta; piketMetadata = piketMeta;
        nativeCourseStart = start; nativeCourseEnd = end; stationKokPrefixEnd = kokEnd;
        orderedPiketPrefixCount = orderedPrefix;
        states = new int[signalRows.Length]; teachingOverrides = new bool[signalRows.Length];
        indexById = new Dictionary<int, int>();
        for (int i = 0; i < signalRows.Length; i++) {
            indexById.Add(signalRows[i].Id, i); states[i] = signalRows[i].OriginalState;
        }
    }

    public string SourceName { get { return sourceName; } }
    public string SourcePath { get { return sourcePath; } }
    public string SourceSha256 { get { return sourceSha256; } }
    public double NativeCourseStart { get { return nativeCourseStart; } }
    public double NativeCourseEnd { get { return nativeCourseEnd; } }
    public double StationKokPrefixEnd { get { return stationKokPrefixEnd; } }
    public int OrderedPiketPrefixCount { get { return orderedPiketPrefixCount; } }
    public bool IsLiveHardwareSource { get { return false; } }
    public bool HasOccupancySource { get { return false; } }
    public string ProfileValueSemantics { get { return "Native TProfE Nach/Len/Uklon; Uklon unit not independently proved"; } }
    public long Generation { get { lock (gate) { return generation; } } }
    public bool PassedStop { get { lock (gate) { return passedStop; } } }
    public int PassedStopSignalId { get { lock (gate) { return passedStop ? originalSignals[blockedIndex].Id : -1; } } }
    public TrainerMarkupSourceMetadata LightMetadata { get { return CopyMetadata(lightMetadata); } }
    public TrainerMarkupSourceMetadata ProfileMetadata { get { return CopyMetadata(profileMetadata); } }
    public TrainerMarkupSourceMetadata StationMetadata { get { return CopyMetadata(stationMetadata); } }
    public TrainerMarkupSourceMetadata PiketMetadata { get { return CopyMetadata(piketMetadata); } }

    // Every returned DTO is a copy. Changing a diagnostic snapshot cannot alter
    // the source; SetTeachingSignalState is the explicit state-change channel.
    public TrainerMarkupSignalSnapshot[] Signals {
        get { lock (gate) {
            var result = new TrainerMarkupSignalSnapshot[originalSignals.Length];
            for (int i = 0; i < result.Length; i++) result[i] = SignalSnapshot(i, false);
            return result;
        } }
    }
    public TrainerMarkupSignalSnapshot[] OriginalSignals {
        get { lock (gate) {
            var result = new TrainerMarkupSignalSnapshot[originalSignals.Length];
            for (int i = 0; i < result.Length; i++) result[i] = SignalSnapshot(i, true);
            return result;
        } }
    }
    public TrainerMarkupProfileSnapshot[] Profiles {
        get { return CopyProfiles(profiles); }
    }
    public TrainerMarkupStationSnapshot[] Stations {
        get { return CopyStations(stations); }
    }
    public TrainerMarkupPiketSnapshot[] Pikets {
        get { return CopyPikets(pikets); }
    }

    // SHA pin covers the exact file bytes, including any UTF8 BOM. Original
    // native binaries are not opened here; their parsed source metadata is
    // required and retained with the pinned normalized artifact.
    public static TrainerMarkupSignalSource Load(string normalizedJsonPath, string expectedSha256) {
        if (String.IsNullOrWhiteSpace(normalizedJsonPath)) throw new ArgumentException("Путь разметки не указан", "normalizedJsonPath");
        if (!IsSha256(expectedSha256)) throw new ArgumentException("Нужен SHA256 точного JSON разметки", "expectedSha256");
        string fullPath = Path.GetFullPath(normalizedJsonPath);
        long fileLength = new FileInfo(fullPath).Length;
        Demand(fileLength > 0 && fileLength <= MaximumJsonBytes, "Недопустимый размер JSON разметки");
        byte[] bytes = File.ReadAllBytes(fullPath);
        Demand(bytes.Length == fileLength, "JSON разметки изменился во время чтения");
        string actualSha;
        using (var hasher = SHA256.Create()) actualSha = Hex(hasher.ComputeHash(bytes));
        Demand(String.Equals(actualSha, expectedSha256, StringComparison.OrdinalIgnoreCase), "SHA256 JSON разметки не совпадает");
        string text = new UTF8Encoding(false, true).GetString(bytes);
        if (text.Length > 0 && text[0] == '\uFEFF') text = text.Substring(1);
        var serializer = new JavaScriptSerializer { MaxJsonLength = MaximumJsonBytes, RecursionLimit = 128 };
        var root = AsObject(serializer.DeserializeObject(text), "root");
        Demand(Integer(root, "schema_version") == 1, "Неподдерживаемая версия JSON разметки");
        Demand(Text(root, "artifact_kind") == "native_course_source_data", "Неверный тип JSON разметки");
        var course = Object(root, "course");
        string name = Text(course, "name");
        Demand(!Boolean(course, "same_as_current_pdf_axis"), "Разметка смешана с неподтверждённой осью PDF");
        Demand(!Boolean(course, "live_occupancy_source_available") && !Boolean(course, "raw_current_alsn_source_proved"),
            "Этот источник поддерживает исходные состояния и явный учебный сценарий");
        var manifest = Object(root, "manifest");
        Demand(Text(manifest, "course_name") == name, "Имя курса не совпадает с манифестом");

        var lightSection = Object(root, "lights");
        var profileSection = Object(root, "profile");
        var stationSection = Object(root, "stations");
        var piketSection = Object(root, "pikets");
        var lightRows = Rows(lightSection, "records");
        var profileRows = Rows(profileSection, "records");
        var stationRows = Rows(stationSection, "records");
        var piketRows = Rows(piketSection, "records");
        var lm = Metadata(lightSection, lightRows.Length, ".light");
        var pm = Metadata(profileSection, profileRows.Length, ".profil");
        var sm = Metadata(stationSection, stationRows.Length, ".stan");
        var km = Metadata(piketSection, piketRows.Length, ".pikets");
        BinaryHeader(lightSection, lightRows.Length, 11);
        BinaryHeader(profileSection, profileRows.Length, 14);
        BinaryHeader(piketSection, piketRows.Length, 14);
        Demand(Text(profileSection, "native_record") == "TProfE", "Неизвестная схема профиля");
        Demand(Boolean(profileSection, "native_fields_confirmed"), "Поля профиля не подтверждены");
        Demand(Boolean(stationSection, "schema_confirmed") && Text(stationSection, "native_loader_rva") == "0x0026533C",
            "Схема станций не подтверждена");
        Demand(Text(piketSection, "native_record") == "TPicetBaseClass.LoadFromProfil", "Неизвестная схема пикетажа");

        var signalData = new TrainerMarkupSignalSnapshot[lightRows.Length];
        var ids = new HashSet<int>();
        int nextLightByte = 11;
        Encoding shortNameEncoding = Encoding.GetEncoding(1251, EncoderFallback.ExceptionFallback, DecoderFallback.ExceptionFallback);
        for (int i = 0; i < lightRows.Length; i++) {
            var row = AsObject(lightRows[i], "light");
            int id = Integer(row, "id"), meter = Integer(row, "meter"), state = Integer(row, "state");
            int type = Integer(row, "type"), lens = Integer(row, "lens");
            int availability = Integer(row, "availability"), autoblocking = Integer(row, "autoblocking");
            bool send = Boolean(row, "send_code"), badWay = Boolean(row, "bad_way");
            string signalName = Text(row, "name");
            ValidateState(state);
            Demand(id >= 0 && ids.Add(id), "Повторный или недопустимый ID светофора");
            Demand(meter >= 0 && (i == 0 || meter > signalData[i - 1].Meter), "Координаты светофоров не возрастают строго");
            Demand(type >= 0 && type <= 13 && lens >= 0 && lens <= 17 && availability >= 0 && availability <= 2 &&
                autoblocking >= 0 && autoblocking <= 2, "Неподдерживаемое поле светофора");
            Demand(!badWay, "BadWay требует отдельного подтверждённого native consumer; clean-state модель его не подменяет");
            Demand(Integer(row, "index") == i && Integer(row, "base_version") == 1 && Integer(row, "extension_version") == 1,
                "Неизвестный индекс или формат записи светофора");
            Demand(shortNameEncoding.GetByteCount(signalName) <= 20, "Имя светофора не помещается в подтверждённый shortstr20");
            Demand(Text(row, "state_name") == CleanStateNames[state], "Имя состояния не совпадает с native enum");
            Demand(Integer(row, "native_basic_code_way") == CleanCode(state, send), "CodeWay не совпадает с подтверждённым native mapper");
            int byteLength = checked(24 + signalName.Length * 2);
            Demand(Integer(row, "byte_offset") == nextLightByte && Integer(row, "byte_length") == byteLength,
                "Неполная или противоречивая бинарная запись светофора");
            nextLightByte = checked(nextLightByte + byteLength);
            signalData[i] = new TrainerMarkupSignalSnapshot {
                Id = id, RecordIndex = i, Meter = meter, Name = signalName, Type = type, Lens = lens,
                State = state, OriginalState = state, StateName = CleanStateNames[state], OriginalStateName = CleanStateNames[state],
                Availability = availability, Autoblocking = autoblocking, SendCode = send, BadWay = false,
                StateOrigin = "original_saved_state", NativeBasicCodeWay = CleanCode(state, send)
            };
        }
        Demand(nextLightByte == lm.Bytes, "Не все байты .light представлены в JSON");

        var profileData = new TrainerMarkupProfileSnapshot[profileRows.Length];
        double start = 0, end = 0;
        for (int i = 0; i < profileRows.Length; i++) {
            var row = AsObject(profileRows[i], "profile");
            double nach = Number(row, "Nach"), length = Number(row, "Len"), uklon = Number(row, "Uklon");
            double segmentEnd = nach + length;
            Demand(Finite(segmentEnd) && nach >= 0 && length > 0 && segmentEnd <= Int32.MaxValue,
                "Недопустимые границы профиля");
            Demand(i == 0 || Math.Abs(nach - end) <= .001, "Разрыв или перекрытие native профиля");
            Demand(Integer(row, "index") == i && Integer(row, "byte_offset") == checked(14 + 12 * i) && Integer(row, "byte_length") == 12,
                "Неверная разметка байтов TProfE");
            profileData[i] = new TrainerMarkupProfileSnapshot { RecordIndex = i, Nach = nach, Len = length, Uklon = uklon };
            if (i == 0) start = nach;
            end = segmentEnd;
        }
        Demand(checked(14 + profileRows.Length * 12) == pm.Bytes && end > start, "Неполный native профиль");
        for (int i = 0; i < signalData.Length; i++) Demand(signalData[i].Meter >= start && signalData[i].Meter <= end, "Светофор вне native профиля");

        var stationData = new TrainerMarkupStationSnapshot[stationRows.Length];
        var stationIds = new HashSet<int>();
        double kokEnd = -1;
        int previousStationByteEnd = 0;
        for (int i = 0; i < stationRows.Length; i++) {
            var row = AsObject(stationRows[i], "station");
            var station = new TrainerMarkupStationSnapshot {
                Id = Integer(row, "id"), Meter = Integer(row, "meter"), Name = Text(row, "name"),
                IdVhodLight = Integer(row, "IdVhodLight"), IdVyhodLight = Integer(row, "IdVyhodLight"),
                IdVhodArrow = Integer(row, "IdVhodArrow"), IdVyhodArrow = Integer(row, "IdVyhodArrow"),
                WayNumber = TextAllowEmpty(row, "WayNumber"), WayTyp = Integer(row, "WayTyp")
            };
            Demand(station.Id >= 0 && stationIds.Add(station.Id) && Integer(row, "index") == i,
                "Повторный или недопустимый индекс станции");
            Demand(station.Meter >= start && station.Meter <= end && (i == 0 || station.Meter > stationData[i - 1].Meter),
                "Недопустимые координаты станции");
            Demand((station.IdVhodLight == -1 || ids.Contains(station.IdVhodLight)) &&
                   (station.IdVyhodLight == -1 || ids.Contains(station.IdVyhodLight)), "Станция ссылается на отсутствующий светофор");
            int at = Integer(row, "byte_offset"), size = Integer(row, "byte_length");
            Demand(at >= 0 && size > 0 && (i == 0 || at == previousStationByteEnd) && Integer(row, "line_start") == 3 + 8 * i,
                "Неполная станционная текстовая запись");
            previousStationByteEnd = checked(at + size);
            if (station.Name == "Кокшаровский") { Demand(kokEnd < 0, "Повторная станция Кокшаровский"); kokEnd = station.Meter; }
            stationData[i] = station;
        }
        Demand(previousStationByteEnd == sm.Bytes, "Не весь .stan представлен в JSON");
        Demand(kokEnd == 191500 && Number(course, "koksharovsky_station_meter") == kokEnd,
            "Не подтверждена отдельная native граница Кокшаровского191500 м");

        var piketData = new TrainerMarkupPiketSnapshot[piketRows.Length];
        int orderedPrefix = piketRows.Length;
        for (int i = 0; i < piketRows.Length; i++) {
            var row = AsObject(piketRows[i], "piket");
            int direction = Integer(row, "Direction");
            var piket = new TrainerMarkupPiketSnapshot {
                RecordIndex = Integer(row, "index"), RecordVersion = Integer(row, "record_version"),
                KM = Integer(row, "KM"), Picet = Integer(row, "Picet"), Offset = Integer(row, "Offset"), Direction = direction,
                DirectionName = Text(row, "Direction_name")
            };
            Demand(piket.RecordIndex == i && piket.RecordVersion == 1 && direction >= 0 && direction <= 1,
                "Неподдерживаемая пикетажная запись");
            Demand(piket.DirectionName == (direction == 0 ? "d_even" : "d_odd"), "Имя направления пикета не совпадает с native enum");
            Demand(piket.Offset >= 0 && piket.Offset <= end && Integer(row, "byte_offset") == checked(14 + 17 * i) &&
                   Integer(row, "byte_length") == 17, "Недопустимые байты или смещение пикета");
            if (i > 0 && piket.Offset <= piketData[i - 1].Offset && orderedPrefix == piketRows.Length) orderedPrefix = i;
            piketData[i] = piket;
        }
        Demand(checked(14 + 17 * piketRows.Length) == km.Bytes, "Не все байты .pikets представлены в JSON");
        Demand(Integer(piketSection, "ordered_prefix_count") == orderedPrefix &&
            Boolean(piketSection, "offsets_strictly_increasing") == (orderedPrefix == piketRows.Length), "Пикетажная anomaly metadata противоречит данным");
        // The observed final Offset0 is retained. No RealKM projection, sort or
        // terminal-offset repair is performed by this source model.
        return new TrainerMarkupSignalSource(name, fullPath, actualSha, signalData, profileData, stationData, piketData,
            lm, pm, sm, km, start, end, kokEnd, orderedPrefix);
    }

    public void SetTeachingSignalState(int id, int state) {
        ValidateState(state);
        lock (gate) {
            int index = FindId(id);
            if (teachingOverrides[index] && states[index] == state) return;
            states[index] = state; teachingOverrides[index] = true; AdvanceGeneration();
            // Changing the signal cannot clear an already latched passed stop.
        }
    }

    public void ClearTeachingSignalState(int id) {
        lock (gate) {
            int index = FindId(id);
            if (!teachingOverrides[index]) return;
            states[index] = originalSignals[index].OriginalState;
            teachingOverrides[index] = false; AdvanceGeneration();
        }
    }

    public void RestorePersistedSignalStates() {
        lock (gate) {
            bool changed = false;
            for (int i = 0; i < states.Length; i++) {
                changed |= teachingOverrides[i]; states[i] = originalSignals[i].OriginalState;
                teachingOverrides[i] = false;
            }
            if (changed) AdvanceGeneration();
        }
    }

    // Explicit new-scenario/teleport source reset. It never resets KLUB/SAUT,
    // their native protection latch, physical position or operator controls.
    public void ResetPosition(double nativeX) {
        ValidatePosition(nativeX);
        lock (gate) {
            lastPosition = nativeX; hasPosition = true;
            passedStop = false; blockedIndex = -1; blockedState = -1;
            AdvanceGeneration();
        }
    }

    public TrainerMarkupSourceFrame Sample(double nativeX) {
        ValidatePosition(nativeX);
        lock (gate) {
            if (hasPosition && nativeX < lastPosition)
                throw new InvalidOperationException("Обратное движение источником не поддержано; нужен явный ResetPosition нового сценария");
            // First sampling establishes the initial baseline, rather than
            // retrospectively interpreting every red signal behind the train.
            if (hasPosition && !passedStop) {
                int first = FirstAtOrAfter(lastPosition);
                for (int i = first; i < originalSignals.Length && originalSignals[i].Meter < nativeX; i++) {
                    if (originalSignals[i].SendCode && IsStopState(states[i])) {
                        passedStop = true; blockedIndex = i; blockedState = states[i]; AdvanceGeneration(); break;
                    }
                }
            }
            int target = passedStop ? blockedIndex : FirstAtOrAfter(nativeX);
            if (target >= originalSignals.Length)
                throw new InvalidOperationException("Нет подтверждённого путевого светофора впереди; код разрешающего сигнала не подставляется");
            var signal = originalSignals[target];
            var frame = new TrainerMarkupSourceFrame {
                Index = signal.Id, Name = signal.Name, Xlight = signal.Meter,
                XlastLight = target == 0 ? 0 : originalSignals[target - 1].Meter,
                SendCode = signal.SendCode, BlocUch = 0, NativeX = nativeX,
                State = states[target], StateName = CleanStateNames[states[target]], OriginalState = signal.OriginalState,
                TeachingOverride = teachingOverrides[target],
                SignalStateOrigin = teachingOverrides[target] ? "explicit_teaching_override" : "original_saved_state",
                CodeWay = passedStop ? (byte)0 : CleanCode(states[target], signal.SendCode),
                CodeWayOrigin = passedStop ? "source_passed_stop_latch" : "native_basic_clean_state_map",
                PassedStop = passedStop, PassedStopSignalId = passedStop ? signal.Id : -1,
                PassedStopState = passedStop ? blockedState : -1, Generation = generation
            };
            lastPosition = nativeX; hasPosition = true;
            return frame;
        }
    }

    public TrainerMarkupSignalSource Clone() {
        lock (gate) {
            var copiedSignals = new TrainerMarkupSignalSnapshot[originalSignals.Length];
            for (int i = 0; i < copiedSignals.Length; i++) copiedSignals[i] = SignalSnapshot(i, true);
            var result = new TrainerMarkupSignalSource(sourceName, sourcePath, sourceSha256,
                copiedSignals, CopyProfiles(profiles), CopyStations(stations), CopyPikets(pikets),
                CopyMetadata(lightMetadata), CopyMetadata(profileMetadata), CopyMetadata(stationMetadata), CopyMetadata(piketMetadata),
                nativeCourseStart, nativeCourseEnd, stationKokPrefixEnd, orderedPiketPrefixCount);
            Array.Copy(states, result.states, states.Length);
            Array.Copy(teachingOverrides, result.teachingOverrides, teachingOverrides.Length);
            result.hasPosition = hasPosition; result.lastPosition = lastPosition;
            result.passedStop = passedStop; result.blockedIndex = blockedIndex; result.blockedState = blockedState;
            result.generation = generation;
            return result;
        }
    }

    int FindId(int id) {
        int result;
        if (!indexById.TryGetValue(id, out result)) throw new ArgumentOutOfRangeException("id", "Нет такого ID светофора в разметке");
        return result;
    }
    int FirstAtOrAfter(double nativeX) {
        int low = 0, high = originalSignals.Length;
        while (low < high) {
            int middle = low + (high - low) / 2;
            if (originalSignals[middle].Meter < nativeX) low = middle + 1;
            else high = middle;
        }
        return low;
    }
    void ValidatePosition(double nativeX) {
        if (!Finite(nativeX) || nativeX < nativeCourseStart || nativeX > nativeCourseEnd)
            throw new ArgumentOutOfRangeException("nativeX", "Позиция вне подтверждённой native оси курса");
    }
    void AdvanceGeneration() {
        if (generation == Int64.MaxValue) throw new InvalidOperationException("Исчерпано поколение источника разметки");
        generation++;
    }
    static bool IsStopState(int state) {
        // These native clean-state red aspects all encode3. Permission to pass
        // them is not invented here; an explicit scenario change precedes a pass.
        return state == 1 || state == 14 || state == 15 || state == 23 || state == 24;
    }
    static byte CleanCode(int state, bool sendCode) {
        ValidateState(state);
        return sendCode ? (byte)CleanStateCodes[state] : (byte)0;
    }
    static void ValidateState(int state) {
        if (state < 0 || state >= CleanStateCodes.Length)
            throw new ArgumentOutOfRangeException("state", "Не подтверждён native clean-state кодировщик этого состояния");
    }
    TrainerMarkupSignalSnapshot SignalSnapshot(int index, bool original) {
        var source = originalSignals[index];
        int state = original ? source.OriginalState : states[index];
        bool changed = !original && teachingOverrides[index];
        return new TrainerMarkupSignalSnapshot {
            Id = source.Id, RecordIndex = source.RecordIndex, Meter = source.Meter, Name = source.Name,
            Type = source.Type, Lens = source.Lens, State = state, OriginalState = source.OriginalState,
            Availability = source.Availability, Autoblocking = source.Autoblocking,
            SendCode = source.SendCode, BadWay = source.BadWay, TeachingOverride = changed,
            StateName = CleanStateNames[state], OriginalStateName = CleanStateNames[source.OriginalState],
            StateOrigin = changed ? "explicit_teaching_override" : "original_saved_state",
            NativeBasicCodeWay = CleanCode(state, source.SendCode), Generation = generation
        };
    }
    static TrainerMarkupProfileSnapshot[] CopyProfiles(TrainerMarkupProfileSnapshot[] input) {
        var result = new TrainerMarkupProfileSnapshot[input.Length];
        for (int i = 0; i < result.Length; i++) result[i] = new TrainerMarkupProfileSnapshot {
            RecordIndex = input[i].RecordIndex, Nach = input[i].Nach, Len = input[i].Len, Uklon = input[i].Uklon
        };
        return result;
    }
    static TrainerMarkupStationSnapshot[] CopyStations(TrainerMarkupStationSnapshot[] input) {
        var result = new TrainerMarkupStationSnapshot[input.Length];
        for (int i = 0; i < result.Length; i++) result[i] = new TrainerMarkupStationSnapshot {
            Id = input[i].Id, Meter = input[i].Meter, Name = input[i].Name, IdVhodLight = input[i].IdVhodLight,
            IdVyhodLight = input[i].IdVyhodLight, IdVhodArrow = input[i].IdVhodArrow,
            IdVyhodArrow = input[i].IdVyhodArrow, WayNumber = input[i].WayNumber, WayTyp = input[i].WayTyp
        };
        return result;
    }
    static TrainerMarkupPiketSnapshot[] CopyPikets(TrainerMarkupPiketSnapshot[] input) {
        var result = new TrainerMarkupPiketSnapshot[input.Length];
        for (int i = 0; i < result.Length; i++) result[i] = new TrainerMarkupPiketSnapshot {
            RecordIndex = input[i].RecordIndex, RecordVersion = input[i].RecordVersion, KM = input[i].KM,
            Picet = input[i].Picet, Offset = input[i].Offset, Direction = input[i].Direction, DirectionName = input[i].DirectionName
        };
        return result;
    }
    static TrainerMarkupSourceMetadata CopyMetadata(TrainerMarkupSourceMetadata input) {
        return new TrainerMarkupSourceMetadata { Path = input.Path, Sha256 = input.Sha256,
            ImportManifestRelativePath = input.ImportManifestRelativePath, HashStatus = input.HashStatus,
            Bytes = input.Bytes, Count = input.Count, FullConsumedSize = input.FullConsumedSize };
    }
    static TrainerMarkupSourceMetadata Metadata(Dictionary<string, object> section, int records, string extension) {
        Demand(records > 0 && Integer(section, "count") == records, "Неполный массив " + extension);
        var source = Object(section, "source");
        var result = new TrainerMarkupSourceMetadata {
            Path = Text(source, "path"), Sha256 = Text(source, "sha256"), Bytes = Integer(source, "bytes"),
            ImportManifestRelativePath = Text(source, "import_manifest_relative_path"), HashStatus = Text(source, "hash_status"),
            Count = records, FullConsumedSize = Integer(section, "full_consumed_size")
        };
        Demand(Path.IsPathRooted(result.Path) && result.Path.EndsWith(extension, StringComparison.OrdinalIgnoreCase) &&
            result.ImportManifestRelativePath.EndsWith(extension, StringComparison.OrdinalIgnoreCase) &&
            IsSha256(result.Sha256) && result.Bytes > 0 && result.FullConsumedSize == result.Bytes &&
            result.HashStatus == "measured_now_and_matches_import_manifest", "Неподтверждённые source metadata " + extension);
        Text(source, "workspace_relative_path");
        return result;
    }
    static void BinaryHeader(Dictionary<string, object> section, int count, int bytes) {
        var header = Object(section, "header");
        Demand(Text(header, "magic") == "New" && Integer(header, "version") == 1 && Integer(header, "count") == count &&
            Integer(header, "header_bytes") == bytes && Text(header, "endianness") == "little", "Неподдерживаемый native binary header");
    }
    static Dictionary<string, object> AsObject(object value, string name) {
        var result = value as Dictionary<string, object>;
        Demand(result != null, "Нет JSON объекта " + name);
        return result;
    }
    static object Value(Dictionary<string, object> parent, string name) {
        object value;
        Demand(parent.TryGetValue(name, out value) && value != null, "Нет JSON поля " + name);
        return value;
    }
    static Dictionary<string, object> Object(Dictionary<string, object> parent, string name) {
        return AsObject(Value(parent, name), name);
    }
    static object[] Rows(Dictionary<string, object> parent, string name) {
        var result = Value(parent, name) as object[];
        Demand(result != null, "Нет JSON массива " + name);
        return result;
    }
    static string TextAllowEmpty(Dictionary<string, object> parent, string name) {
        var result = Value(parent, name) as string;
        Demand(result != null, "JSON поле должно быть строкой: " + name);
        return result;
    }
    static string Text(Dictionary<string, object> parent, string name) {
        string result = TextAllowEmpty(parent, name);
        Demand(!String.IsNullOrWhiteSpace(result), "Пустое JSON поле " + name);
        return result;
    }
    static bool Boolean(Dictionary<string, object> parent, string name) {
        object value = Value(parent, name);
        Demand(value is bool, "JSON поле должно быть Boolean: " + name);
        return (bool)value;
    }
    static double Number(Dictionary<string, object> parent, string name) {
        object value = Value(parent, name);
        Demand(value is int || value is long || value is double || value is decimal || value is float,
            "JSON поле должно быть числом: " + name);
        double result = Convert.ToDouble(value, CultureInfo.InvariantCulture);
        Demand(Finite(result), "Недопустимое JSON число " + name);
        return result;
    }
    static int Integer(Dictionary<string, object> parent, string name) {
        double value = Number(parent, name);
        Demand(value >= Int32.MinValue && value <= Int32.MaxValue && Math.Truncate(value) == value,
            "JSON поле должно быть Int32: " + name);
        return (int)value;
    }
    static bool Finite(double value) {
        // Bit test avoids constructing NaN/Infinity sentinels under native FPU
        // exception masks. The class never changes global FPU configuration.
        return (((ulong)BitConverter.DoubleToInt64Bits(value)) & 0x7FF0000000000000UL) != 0x7FF0000000000000UL;
    }
    static bool IsSha256(string value) {
        if (value == null || value.Length != 64) return false;
        for (int i = 0; i < value.Length; i++) {
            char c = value[i];
            if (!(c >= '0' && c <= '9') && !(c >= 'a' && c <= 'f') && !(c >= 'A' && c <= 'F')) return false;
        }
        return true;
    }
    static string Hex(byte[] value) {
        var result = new StringBuilder(value.Length * 2);
        for (int i = 0; i < value.Length; i++) result.Append(value[i].ToString("x2", CultureInfo.InvariantCulture));
        return result.ToString();
    }
    static void Demand(bool condition, string reason) {
        if (!condition) throw new InvalidDataException(reason);
    }
}
