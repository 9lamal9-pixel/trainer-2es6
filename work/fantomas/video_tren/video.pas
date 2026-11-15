unit video;

interface
uses  AVI,sysutils,Exfiles,classes,Tren_Classes,Tren_Classes2,
Dialogs,D3DX8;
Procedure CalculateAvi;
Procedure CalculateAviSpeed;
Procedure LoadExFile(name:string);
Procedure SelectAviFile;
Procedure PrepareObjects;
Function GetAlias(al:string):integer;
Procedure LoadAliases;
Procedure LoadBDATFile(Filename:string);
var
  VDE_Version:string;
implementation
uses Videofrm,Unit1,Lighters;

Procedure CalculateAvi;
var i:integer;
begin
avilenght:=0;
  for i:=0 to AviFileList.count-1 do begin
   TMyAviFile(AviFileList.items[i]).offset:=avilenght;
   avilenght:=avilenght+TMyAviFile(AviFileList.items[i]).length;
  end;

end;

Procedure CalculateAviSpeed;
var
  i:integer;
  pprev,pnext,pprev1,pnext1:integer;
  k1,k2,k4:real;
begin
  AviSpeed:=60;
  with form1 do
    begin
      if Overalframe=0 then Overalframe:=1;
      i:=GetPiket(Overalframe);
      if VDE_Version='VDS27' then VisPikets.SelectPiket(i);
      //if i=-1 then i:=0;
      //RealKmpik:=TPiket(Pikets.Items[i]).number;
      pnext:=strtoint(pikets.items[i+1]);
      pprev:=strtoint(pikets.items[i]);
      if i<>-1 then
        begin
        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
          if (i=0) or (i=pikets.items.count-2) then
            begin
              if propuskpiket then
                if i=0 then
                  begin
                    pnext:=strtoint(pikets.items[i+2]);pprev:=strtoint(pikets.items[i+1]);
                  end;
              AviSpeed:=(1/(((pnext-pprev)*10)/AviFPS))*3600;
            end
            else
              begin
                if Overalframe<=round(pnext-((pnext-pprev)/2)) then
                  begin
                    if (i<>0) or (not propuskpiket) then
                      begin
                        pnext1:=strtoint(pikets.items[i]);
                        pprev1:=strtoint(pikets.items[i-1]);
                        k1:=(pnext-((pnext-pprev)/2))-(pnext1-((pnext1-pprev1)/2));
                        k2:=((1/(((pnext-pprev)*10)/AviFPS))*3600)-((1/(((pnext1-pprev1)*10)/AviFPS))*3600);
                        k4:=Overalframe-(pnext1-((pnext1-pprev1)/2));
                        AviSpeed:=abs((k2*k4)/k1+((1/(((pnext1-pprev1)*10)/AviFPS))*3600));
                      end
                  end else
                        begin
                          pnext1:=pnext;
                          pprev1:=pprev;
                          pnext:=strtoint(pikets.items[i+2]);
                          pprev:=strtoint(pikets.items[i+1]);
                          k1:=(pnext-((pnext-pprev)/2))-(pnext1-((pnext1-pprev1)/2));
                          k2:=((1/(((pnext-pprev)*10)/AviFPS))*3600)-((1/(((pnext1-pprev1)*10)/AviFPS))*3600);
                          k4:=Overalframe-(pnext1-((pnext1-pprev1)/2));
                          AviSpeed:=abs((k2*k4)/k1+((1/(((pnext1-pprev1)*10)/AviFPS))*3600));
                        end;
              end;
        end
        else AviSpeed:=60;
    end; // po with
end;

Procedure LoadExFile(name:string);
var f:textfile;
s:string;
i,p:integer;
VersionType:byte;
scale:single;
begin
  chdir(datapath);
  AssignFile(f,Name);
  reset(f);
  obj:=TEXFile.create;

    obj.name:='';
    obj.FileName:=ExtractFileName(Name);
    Readln(f,s);
    if s='EX FILE by DSL of PI 2001(c)' then VersionType:=1;
    if s='EX FILE by DSL of PI 2002(c)' then VersionType:=2;
    if s='EX FILE by DSL of PI 2002(c) [b1]' then VersionType:=3;
if Versiontype>2 then Readln(f,Obj.name);
    Readln(f,s);
    obj.LoadMeshFromX(projectpath+datapath+s);
     Readln(f,p);
      if p>0 then
       for i:=0 to p-1 do begin
    Readln(f,Obj.NameMaterials[i]);
    Readln(f,Obj.Transparency[i]);
    Readln(f,Obj.Visible[i]);
if Versiontype>1 then begin
    Readln(f,Obj.SnowHat[i]);
    Readln(f,Obj.Reserved[i]);
end;

    Readln(f,Obj.MeshMaterials[i].ambient.r);
    readln(f,Obj.MeshMaterials[i].ambient.g);
    readln(f,Obj.MeshMaterials[i].ambient.b);
    readln(f,Obj.MeshMaterials[i].ambient.a);
    readln(f,Obj.MeshMaterials[i].Diffuse.r);
    readln(f,Obj.MeshMaterials[i].Diffuse.g);
    readln(f,Obj.MeshMaterials[i].Diffuse.b);
    readln(f,Obj.MeshMaterials[i].Diffuse.a);
    readln(f,Obj.MeshMaterials[i].Specular.r);
    readln(f,Obj.MeshMaterials[i].Specular.g);
    readln(f,Obj.MeshMaterials[i].Specular.b);
    readln(f,Obj.MeshMaterials[i].Specular.a);
    readln(f,Obj.MeshMaterials[i].Emissive.r);
    readln(f,Obj.MeshMaterials[i].Emissive.g);
    readln(f,Obj.MeshMaterials[i].Emissive.b);
    readln(f,Obj.MeshMaterials[i].Emissive.a);

    readln(f,Obj.MeshMaterials2[i].ambient.r);
    readln(f,Obj.MeshMaterials2[i].ambient.g);
    readln(f,Obj.MeshMaterials2[i].ambient.b);
    readln(f,Obj.MeshMaterials2[i].ambient.a);
    readln(f,Obj.MeshMaterials2[i].Diffuse.r);
    readln(f,Obj.MeshMaterials2[i].Diffuse.g);
    readln(f,Obj.MeshMaterials2[i].Diffuse.b);
    readln(f,Obj.MeshMaterials2[i].Diffuse.a);
    readln(f,Obj.MeshMaterials2[i].Specular.r);
    readln(f,Obj.MeshMaterials2[i].Specular.g);
    readln(f,Obj.MeshMaterials2[i].Specular.b);
    readln(f,Obj.MeshMaterials2[i].Specular.a);
    readln(f,Obj.MeshMaterials2[i].Emissive.r);
    readln(f,Obj.MeshMaterials2[i].Emissive.g);
    readln(f,Obj.MeshMaterials2[i].Emissive.b);
    readln(f,Obj.MeshMaterials2[i].Emissive.a);


       end;
ChDir(ProjectPath);
scale:=0.01;
d3dxmatrixscaling(obj.WorldMatrix.Scale,0.75*scale,scale,scale);
d3dxMatrixTranslation(obj.WorldMatrix.Pos,0,0,0.1);
Obj.Pos.Z:=5;
Obj.CompileMatrix;

CloseFile(f);
end;

Procedure SelectAviFile;
begin
if Overalframe>CurAviFile.Length+CurAviFile.offset then
if CurAviFileIndex<AviFileList.count-1 then begin
 inc(CurAviFileIndex);
 curAviFile:=AviFileList.items[CurAviFileIndex];
 CloseAvi;
 InitAvi(curAviFile.FullFileName);
VideoFrm.realframe:=CurAviFile.StartPos;
 form1.listbox4.itemindex:=CurAviFileIndex;
 end else played:=false;
//*********************************************************************************************
//*********************************************************************************************
//                       TUT MOGLA BIT' VASHA REKLAMA !!!
//*********************************************************************************************
//*********************************************************************************************
if Overalframe<CurAviFile.offset then
if CurAviFileIndex>0 then begin
 dec(CurAviFileIndex);
 curAviFile:=AviFileList.items[CurAviFileIndex];
 CloseAvi;
 InitAvi(curAviFile.FullFileName);
VideoFrm.realframe:=CurAviFile.endpos;
 form1.listbox4.itemindex:=CurAviFileIndex;
 end else played:=false;

end;

Procedure PrepareObjects;
var i:integer;
begin
if T3DObjList.count>0 then
  for i:=0 to T3DObjList.count-1 do begin
   TSimpleObj(T3DObjList.items[i]).CurKm:=kader2meter(TSimpleObj(T3DObjList.items[i]).endframe)/1000;
   TSimpleObj(T3DObjList.items[i]).started:=false;
  end;
end;

Function GetAlias(al:string):integer;
var i:integer;
begin
for i:=0 to form1.combobox3.items.count-1 do
if al=form1.combobox3.items[i] then result:=i;
end;

Procedure LoadAliases;
var
fa:textfile;
tempstr:string;
begin
assignfile(fa,'aliases.ini');
reset(fa);
readln(fa,tempstr);
aliaslist.clear;
form1.combobox3.clear;
while not EOF(fa) do begin
readln(fa,tempstr);
form1.combobox3.items.add(tempstr);
readln(fa,tempstr);
aliaslist.add(tempstr);
end;
form1.combobox3.text:=form1.combobox3.items[0];
end;

Procedure LoadBDATFile(Filename:string);
var
  f:file;
  strcount,c1,kcount,lcount,pcount,i,p,k:integer;
  temp,version:string;
  fileid:array[1..5] of char;
  tmpl:Ttlighter;
  o:TsimpleObj;
  avi:TmyAviFile;
  tmph:Thelper;
  tmpbool,tb:byte;
  mypic:Tpiket;
begin

if AviFileList=nil then AviFileList:=Tlist.create;
temp:='';
assignfile(f,filename);
Reset(f,1);
blockread(f,fileid,5);
for c1:=1 to 5 do temp:=temp+fileid[c1];
version:=temp;
VDE_Version:=temp;
if (version='VDS24') or (version='VDS25')or(version='VDS26')or(version='VDS27') then begin
// Загрузка AVI афйлов с параметрами
blockread(f,p,sizeof(p));
if p>0 then
 for i:=0 to p-1 do begin
 Avi:=TmyAviFile.create;
 blockread(f,tmpbool,1);
 for strcount:=1 to tmpbool do begin
   blockread(f,tb,1);avi.filename:=avi.filename+chr(tb);
 end;
 blockread(f,avi.AliasID,sizeof(avi.AliasID));
 blockread(f,avi.offset,sizeof(avi.offset));
// blockread(f,avi.drawbox,sizeof(avi.drawbox));
 blockread(f,strcount,sizeof(strcount));
 avi.startpos:=strcount;
 blockread(f,strcount,sizeof(strcount));
 avi.endpos:=strcount;
 blockread(f,strcount,sizeof(strcount));
 avi.MaxLength:=strcount;
 avi.fullfilename:=aliaslist.strings[avi.aliasID]+avi.filename;
 avifilelist.add(avi);
 form1.listbox4.items.add(form1.combobox3.items[avi.aliasID]+'\'+avi.filename);
 end;

form1.progressbar1.position:=form1.progressbar1.position+1;

if avifilelist.count>0 then begin
CurAvifile:=TMyAviFile(AviFilelist.items[0]);
avifilename:=curavifile.FullFileName;
end;


blockread(f,tmpbool,1);
if tmpbool=0 then PropuskPiket:=true else PropuskPiket:=False;
blockread(f,p,sizeof(p));
form1.pikets.clear;
for i:=0 to p-1 do begin
blockread(f,strcount,sizeof(strcount));
mypic:=Tpiket.create;
mypic.fdata.d:=strcount;
form1.pikets.items.AddObject(inttostR(mypic.value),mypic);
end;
form1.progressbar1.position:=form1.progressbar1.position+1;
// Загрузка светофора
for i:=0 to TlightersList.count-1 do
Ttlighter(Tlighterslist.items[i]).destroy;
TlightersList.clear;
blockread(f,lcount,sizeof(lcount));
for i:=0 to lcount-1 do begin
 tmpl:=TTLighter.Create;
 Tlighterslist.add(tmpl);
 tmpl.NextLighter:=nil;
  if i=0 then tmpl.PrevLighter:=nil else begin
   tmpl.PrevLighter:=TTLighter(TLightersList.items[i-1]);
   TTLighter(TLightersList.items[i-1]).NextLighter:=tmpl;
  end;

 tmpl.PosFrame:=i;
 blockread(f,tmpl.Startframe,sizeof(tmpl.Startframe));
 blockread(f,tmpl.EndFrame,sizeof(tmpl.EndFrame));
 blockread(f,tmpbool,1);
  for strcount:=1 to tmpbool do begin
    blockread(f,tb,1);tmpl.Name:=tmpl.Name+chr(tb);
  end;
 blockread(f,tmpl.LType,sizeof(tmpl.LType));
 blockread(f,tmpl.VisibleRange,sizeof(tmpl.VisibleRange));
 blockread(f,tmpBool,sizeof(tmpbool));
 if tmpbool=0 then tmpl.SendCode:=true else tmpl.SendCode:=false;
 blockread(f,kcount,sizeof(kcount));
  for k:=0 to kcount-1 do begin
  tmpl.Lights.Add(TTLLight.Create);
   with TTLLight(tmpl.Lights[k]) do begin
   blockread(f,color,sizeof(color));
   blockread(f,state,sizeof(state));
   blockread(f,pcount,sizeof(pcount));
    for p:=0 to Pcount-1 do begin
    posscale.add(TLTstate.create(0,0,0,true));
    blockread(f,TltState(PosScale.Items[p]).st,sizeof(TltState(PosScale.Items[p]).st));
    blockread(f,tmpbool,1);
    if tmpbool=0 then TltState(PosScale.Items[p]).visible:=true else TltState(PosScale.Items[p]).visible:=false;
    end;
   end;
  end;
CompileLighter(tmpl);
end;


if Tlighterslist.count>0 then CurTrafficLighter:=Ttlighter(Tlighterslist.items[0]);
// end of Загрузка светофора

form1.progressbar1.position:=form1.progressbar1.position+1;
// Загрузка библиотеки обьектов
BlockRead(f,kCount,Sizeof(kCount));
if kCount>0 then begin
  if ObjList.count>0 then
    for i:=0 to ObjList.count -1 do
     TexFile(ObjList.items[i]).destroy;
  ObjList.clear;
  for i:=0 to kCount-1 do begin
    blockread(f,tmpbool,1);
    temp:='';
      for strcount:=1 to tmpbool do begin
        blockread(f,tb,1);temp:=temp+chr(tb);
      end;
    LoadExFile(temp);
    ObjList.add(obj);
 end;
end;
//   RepaintObjList(ObjList.count-1);

form1.progressbar1.position:=form1.progressbar1.position+1;

// Загрузка объектов
  BlockRead(f,kCount,Sizeof(kCount));
  if kCount>0 then
    begin
      if T3DObjList.count>0 then
        for i:=0 to T3DObjList.count -1 do TSimpleObj(T3DObjList.items[i]).destroy;
      T3DObjList.clear;
      for i:=0 to kCount-1 do
        begin
          o:=TsimpleObj.create;
          BlockRead(f,o.EX_ID,SizeOf(o.EX_ID));
          o.EX_Obj:=TexFIle(ObjList.items[o.ex_ID]);
          blockread(f,tmpbool,1);
          for strcount:=1 to tmpbool do
            blockread(f,tb,1);o.Name:=o.Name+chr(tb);
          BlockRead(f,o.StartFrame,SizeOf(o.StartFrame));
          BlockRead(f,o.EndFrame,SizeOf(o.EndFrame));
          BlockRead(f,o.KeepLeft,SizeOf(o.KeepLeft));
          BlockRead(f,o.KeepRight,SizeOf(o.KeepRight));
          BlockRead(f,pCount,SizeOf(pCount));
          SetLength(o.PosState,pcount);
          BlockRead(f,o.PosState[0],pCount*SizeOf(ObjState));
          SetLength(o.PosState2,pcount);
          BlockRead(f,o.PosState2[0],pCount*SizeOf(ObjState));
          SetLength(o.PosState3,pcount);
          BlockRead(f,o.PosState3[0],pCount*SizeOf(ObjState));
          if (version='VDS26') or(version='VDS27')then   //Последствия столкновения.Загрузка
            BlockRead(f,o.State,SizeOf(o.State));
          T3DObjList.add(o);
        end;
    end;
//RefreshT3DObjList(t3dObjList.count);

if (version='VDS25')or (version='VDS26') or(version='VDS27')then begin
// загрузка хелперов
BlockRead(f,kCount,sizeof(kCount));
 if kCount>0 then begin
    if helperslist.count>0 then
        for i:=0 to Helperslist.Count-1 do
          Thelper(helperslist.items[i]).destroy;
    helperslist.clear;
  for i:=0 to kCount-1 do begin // label 1
   tmph:=thelper.create;
     blockread(f,tmpbool,1);
     for strcount:=1 to tmpbool do begin
      blockread(f,tb,1);tmph.Name:=tmph.Name+chr(tb);
     end;
    BlockRead(f,tmpbool,1);
    if tmpbool=0 then tmph.left:=true else tmph.left:=false;
    BlockRead(f,tmph.StartFrame,sizeof(tmph.StartFrame));
    BlockRead(f,tmph.EndFrame,sizeof(tmph.EndFrame));
    BlockRead(f,p,sizeof(p));
    SetLength(tmph.posstate,p);
    BlockRead(f,tmph.posstate[0],p*Sizeof(HelperState));
    Helperslist.add(tmph);
  end; // label 1
 end; // po kCount
//RefreshHelperList;
end;

Blockread(f,p,sizeof(p));
SetLength(SoundPoints.points,p);
BlockRead(f,SoundPoints.points[0],p*Sizeof(SPoint));
if (version='VDS27') then begin
VisPikets.LoadVisPikets(f); //загрузка отображаемых пикетов
{Pathsigns.LoadSigns(f); //загрузка знаков
main.RedrawListBox;
Strel.OpenStrel_(f);// загрузка стрелок
Main.lbxStrel.Clear;
for i:=1 to strel.KolStrel do Main.lbxStrel.Items.Add(inttostr(i));
Main.lbxStrel.ItemIndex:=0;
if Main.lbxstrel.items.count>0 then  Main.lbxStrel.OnClick(nil);
Krivie.OpenKriv_(f);//Загрузка кривых
Main.lbxKriv.Clear;
for i:=1 to Krivie.KolKriv do Main.lbxKriv.Items.Add(inttostr(i));
Main.lbxKriv.ItemIndex:=0;
if Main.lbxKriv.items.count>0 then Main.lbxKriv.OnClick(nil);   }
end;
CalculateAvi;
InitAvi(CurAviFile.FullFileName);


end else MessageDlg('Incorrect format version. ['+temp+']', mtError,
      [mbOk], 0);

CloseFile(f);

//main.Listbox4.itemindex:=0;
//main.Listbox4click(nil);
end;


end.
