/*
 * Decompiled with CFR 0.152.
 */
package base.sound;

import base.data.BlockData;
import base.sound.SoundPlayer;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SoundController {
    private static final Logger logger = LogManager.getLogger(SoundController.class);
    private static SoundController instance;
    private HashMap<String, String> FILE_NAMES;
    private SoundPlayer sautSoundPlayer = new SoundPlayer("saut");
    private SoundPlayer[] blokSoundPlayers = new SoundPlayer[]{new SoundPlayer("block1"), new SoundPlayer("block2"), new SoundPlayer("block3"), new SoundPlayer("block4"), new SoundPlayer("block5")};
    private SoundPlayer blokCyclicPlayer = new SoundPlayer("cyclic");
    private int blokSoundPlayerIndex = 0;

    public static SoundController getInstance() {
        if (instance == null) {
            instance = new SoundController();
        }
        return instance;
    }

    private SoundController() {
        this.sautSoundPlayer.start();
        for (SoundPlayer soundPlayer : this.blokSoundPlayers) {
            soundPlayer.start();
        }
        this.FILE_NAMES = new HashMap();
        this.FILE_NAMES.put("\u041a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u043f\u0438\u0441\u043a", "Club_Cikl.wav");
        this.FILE_NAMES.put("\u041d\u0430\u0436\u0430\u0442\u0438\u0435 \u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0430\u0442\u0443\u0440\u0443", "key_click.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_1_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_1_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_1_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_1_4.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_2_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_2_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_2_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_2_4.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_3_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_3_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_3_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_3_4.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_4_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_4_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_4_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_4_4.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_5_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_5_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_5_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_5_4.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_6_1.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_6_2.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_6_3.wav");
        this.FILE_NAMES.put("\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Box_6_4.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_BV_1.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_BV_2.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_BV_3.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_BV_4.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_1.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_2.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_3.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_4.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_Vspom_1.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_Vspom_2.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_Vspom_3.wav");
        this.FILE_NAMES.put("\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_DifRele_Vspom_4.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_AB_1.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_AB_2.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_AB_3.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_AB_4.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_Kompr_1.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_Kompr_2.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_Kompr_3.wav");
        this.FILE_NAMES.put("\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Not_Kompr_4.wav");
        this.FILE_NAMES.put("\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Peregruz_1.wav");
        this.FILE_NAMES.put("\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Peregruz_2.wav");
        this.FILE_NAMES.put("\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Peregruz_3.wav");
        this.FILE_NAMES.put("\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438", "SAUT_Peregruz_4.wav");
        this.FILE_NAMES.put("\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 1 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "SAUT_Vent_1_Vykl.wav");
        this.FILE_NAMES.put("\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 2 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "SAUT_Vent_2_Vykl.wav");
        this.FILE_NAMES.put("\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 3 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "SAUT_Vent_3_Vykl.wav");
        this.FILE_NAMES.put("\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 4 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d", "SAUT_Vent_4_Vykl.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043e\u043f\u0430\u0441\u043d\u043e\u0435 \u043c\u0435\u0441\u0442\u043e", "SAUT_OpasnMesto.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043c\u043e\u0441\u0442", "SAUT_Most.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0435\u0440\u0435\u0435\u0437\u0434", "SAUT_Pereezd.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u0430", "SAUT_Platforma.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0442\u043e\u043d\u043d\u0435\u043b\u044c", "SAUT_Tonnel.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0443\u0442\u0435\u043f\u0440\u043e\u0432\u043e\u0434", "SAUT_Puteprovod.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0435\u0440\u0435\u0445\u043e\u0434", "SAUT_Perehod.wav");
        this.FILE_NAMES.put("\u0421\u0438\u0433\u043d\u0430\u043b", "SAUT_Signal.wav");
        this.FILE_NAMES.put("\u041f\u0440\u043e\u0431\u0430 \u0442\u043e\u0440\u043c\u043e\u0437\u043e\u0432", "SAUT_ProbaTorm.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0441\u0442\u0430\u043d\u0446\u0438\u044f", "SAUT_Station.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0433\u0430\u0437\u043e\u043f\u0440\u043e\u0432\u043e\u0434", "SAUT_Gazoprovod.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u043e\u043d\u0430\u0431", "SAUT_Ponab.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0442\u043e\u043a\u043e\u0440\u0430\u0437\u0434\u0435\u043b", "SAUT_Tokorazdel.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043d\u0435\u0439\u0442\u0440\u0430\u043b\u044c\u043d\u0430\u044f \u0432\u0441\u0442\u0430\u0432\u043a\u0430", "SAUT_NeytralVstav.wav");
        this.FILE_NAMES.put("\u041f\u043e\u043d\u0430\u0431 \u0437\u0435\u043b\u0451\u043d\u044b\u0439", "SAUT_PonabGreen.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043f\u043e\u043d\u0430\u0431 \u043a\u0440\u0430\u0441\u043d\u044b\u0439", "SAUT_PonabRed.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043f\u043e\u043d\u0430\u0431 \u0436\u0451\u043b\u0442\u044b\u0439", "SAUT_PonabYellow.wav");
        this.FILE_NAMES.put("\u041f\u043e\u0432\u044b\u0448\u0435\u043d\u043d\u043e\u0435 \u043d\u0430\u043f\u0440\u044f\u0436\u0435\u043d\u0438\u0435 \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043d\u043e\u0439 \u0441\u0435\u0442\u0438", "SAUT_UpKontSet.wav");
        this.FILE_NAMES.put("\u041f\u043e\u043d\u0438\u0436\u0435\u043d\u043d\u043e\u0435 \u043d\u0430\u043f\u0440\u044f\u0436\u0435\u043d\u0438\u0435 \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043d\u043e\u0439 \u0441\u0435\u0442\u0438", "SAUT_LowKontSet.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0437\u0435\u043b\u0451\u043d\u044b\u0439", "SAUT_Green.wav");
        this.FILE_NAMES.put("\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0436\u0451\u043b\u0442\u044b\u0439", "SAUT_VperediYellow.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u0436\u0451\u043b\u0442\u044b\u0439", "SAUT_Yellow.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043a\u0440\u0430\u0441\u043d\u044b\u0439", "SAUT_Red.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u043a\u0440\u0430\u0441\u043d\u044b\u0439", "SAUT_Vperedi_Red.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0431\u0435\u043b\u044b\u0439", "SAUT_White.wav");
        this.FILE_NAMES.put("\u041e\u0442\u043a\u043b\u044e\u0447\u0438 \u0442\u044f\u0433\u0443", "SAUT_OtklTyga.wav");
        this.FILE_NAMES.put("\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043d\u0430\u0447\u0430\u043b\u043e \u0434\u0432\u0438\u0436\u0435\u043d\u0438\u044f", "SAUT_NachaloDvig.wav");
        logger.debug("init");
    }

    private String getFileName(String audioName) {
        return this.FILE_NAMES.get(audioName);
    }

    public void play(String name) {
        String fileName;
        if (BlockData.getInstance().getCurNetworkData().getSautEquipped() == 1 && (fileName = this.getFileName(name)) != null) {
            this.sautSoundPlayer.play(fileName);
        }
    }

    public void playMalfunction(int code) {
        if (BlockData.getInstance().getCurNetworkData().getSautEquipped() == 1) {
            String fileName;
            String malfunctionName = "";
            switch (code) {
                case 0: {
                    break;
                }
                case 1: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 2: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 3: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 4: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0431\u044b\u0441\u0442\u0440\u043e\u0434\u0435\u0439\u0441\u0442\u0432\u0443\u044e\u0449\u0435\u0433\u043e \u043a\u043e\u043d\u0442\u0430\u043a\u0442\u043e\u0440\u0430 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 5: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 6: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 7: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 8: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 9: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 10: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 11: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 12: {
                    malfunctionName = "\u0421\u0440\u0430\u0431\u0430\u0442\u044b\u0432\u0430\u043d\u0438\u0435 \u0434\u0438\u0444\u0440\u0435\u043b\u0435 \u0432\u0441\u043f\u043e\u043c\u0430\u0448\u0438\u043d 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 13: {
                    malfunctionName = "\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 14: {
                    malfunctionName = "\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 15: {
                    malfunctionName = "\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 16: {
                    malfunctionName = "\u041d\u0435\u0442 \u0437\u0430\u0440\u044f\u0434\u0430 \u0431\u0430\u0442\u0430\u0440\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 17: {
                    malfunctionName = "\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 18: {
                    malfunctionName = "\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 19: {
                    malfunctionName = "\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 20: {
                    malfunctionName = "\u041d\u0435\u0437\u0430\u043f\u0443\u0441\u043a \u043a\u043e\u043c\u043f\u0440\u0435\u0441\u0441\u043e\u0440\u0430 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 21: {
                    malfunctionName = "\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 22: {
                    malfunctionName = "\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 23: {
                    malfunctionName = "\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 24: {
                    malfunctionName = "\u041f\u0435\u0440\u0435\u0433\u0440\u0443\u0437\u043a\u0430 \u0434\u0432\u0438\u0433\u0430\u0442\u0435\u043b\u0435\u0439 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 25: {
                    malfunctionName = "\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 1 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
                    break;
                }
                case 26: {
                    malfunctionName = "\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 2 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
                    break;
                }
                case 27: {
                    malfunctionName = "\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 3 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
                    break;
                }
                case 28: {
                    malfunctionName = "\u0412\u0435\u043d\u0442\u0438\u043b\u044f\u0442\u043e\u0440 4 \u0441\u0435\u043a\u0446\u0438\u0438 \u0432\u044b\u043a\u043b\u044e\u0447\u0435\u043d";
                    break;
                }
                case 29: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 30: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 31: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 32: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 1 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 33: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 34: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 35: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 36: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 2 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 37: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 38: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 39: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 40: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 3 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 41: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 42: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 43: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 44: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 4 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 45: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 46: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 47: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 48: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 5 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 49: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 1 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 50: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 2 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 51: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 3 \u0441\u0435\u043a\u0446\u0438\u0438";
                    break;
                }
                case 52: {
                    malfunctionName = "\u0411\u043e\u043a\u0441\u043e\u0432\u0430\u043d\u0438\u0435 6 \u043e\u0441\u0438 4 \u0441\u0435\u043a\u0446\u0438\u0438";
                }
            }
            if (malfunctionName.length() > 0 && (fileName = this.getFileName(malfunctionName)) != null) {
                this.sautSoundPlayer.play(fileName);
            }
        }
    }

    public void playTarget(int code, int trafficLightCode) {
        if (BlockData.getInstance().getCurNetworkData().getSautEquipped() == 1) {
            String fileName;
            String targetName = "";
            switch (code) {
                case 0: {
                    break;
                }
                case 2: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043e\u043f\u0430\u0441\u043d\u043e\u0435 \u043c\u0435\u0441\u0442\u043e";
                    break;
                }
                case 3: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043c\u043e\u0441\u0442";
                    break;
                }
                case 4: {
                    targetName = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0435\u0440\u0435\u0435\u0437\u0434";
                    break;
                }
                case 5: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u043b\u0430\u0442\u0444\u043e\u0440\u043c\u0430";
                    break;
                }
                case 6: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0442\u043e\u043d\u043d\u0435\u043b\u044c";
                    break;
                }
                case 7: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0443\u0442\u0435\u043f\u0440\u043e\u0432\u043e\u0434";
                    break;
                }
                case 8: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u0435\u0440\u0435\u0445\u043e\u0434";
                    break;
                }
                case 10: {
                    targetName = "\u0421\u0438\u0433\u043d\u0430\u043b";
                    break;
                }
                case 11: {
                    targetName = "\u041f\u0440\u043e\u0431\u0430 \u0442\u043e\u0440\u043c\u043e\u0437\u043e\u0432";
                    break;
                }
                case 12: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0441\u0442\u0430\u043d\u0446\u0438\u044f";
                    break;
                }
                case 13: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0433\u0430\u0437\u043e\u043f\u0440\u043e\u0432\u043e\u0434";
                    break;
                }
                case 14: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u043f\u043e\u043d\u0430\u0431";
                }
            }
            if (targetName.length() > 0 && (fileName = this.getFileName(targetName)) != null) {
                this.sautSoundPlayer.play(fileName);
            }
        }
    }

    public void playTrafficLignt(int code) {
        if (BlockData.getInstance().getCurNetworkData().getSautEquipped() == 1) {
            String fileName;
            String targetName = "";
            switch (code) {
                case 0: {
                    targetName = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0431\u0435\u043b\u044b\u0439";
                    break;
                }
                case 1: {
                    targetName = "\u0412\u043f\u0435\u0440\u0435\u0434\u0438 \u0437\u0435\u043b\u0451\u043d\u044b\u0439";
                    break;
                }
                case 2: {
                    targetName = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u0436\u0451\u043b\u0442\u044b\u0439";
                    break;
                }
                case 3: {
                    targetName = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u043a\u0440\u0430\u0441\u043d\u044b\u0439";
                    break;
                }
                case 4: {
                    targetName = "\u0412\u043d\u0438\u043c\u0430\u043d\u0438\u0435 \u0432\u043f\u0435\u0440\u0435\u0434\u0438 \u043a\u0440\u0430\u0441\u043d\u044b\u0439";
                }
            }
            if (targetName.length() > 0 && (fileName = this.getFileName(targetName)) != null) {
                this.sautSoundPlayer.play(fileName);
            }
        }
    }

    public void playClick() {
        String fileName = this.getFileName("\u041d\u0430\u0436\u0430\u0442\u0438\u0435 \u043d\u0430 \u043a\u043b\u0430\u0432\u0438\u0430\u0442\u0443\u0440\u0443");
        if (fileName != null) {
            this.blokSoundPlayers[this.blokSoundPlayerIndex].play(fileName);
            ++this.blokSoundPlayerIndex;
            if (this.blokSoundPlayerIndex >= this.blokSoundPlayers.length) {
                this.blokSoundPlayerIndex = 0;
            }
        }
    }

    public void playBeep() {
        String fileName = this.getFileName("\u041a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u043f\u0438\u0441\u043a");
        if (fileName != null) {
            this.blokSoundPlayers[this.blokSoundPlayerIndex].play(fileName);
            ++this.blokSoundPlayerIndex;
            if (this.blokSoundPlayerIndex >= this.blokSoundPlayers.length) {
                this.blokSoundPlayerIndex = 0;
            }
        }
    }

    public void playCyclicBeep() {
        if (!this.blokCyclicPlayer.isAlive()) {
            this.blokCyclicPlayer = new SoundPlayer("cyclic");
            this.blokCyclicPlayer.start();
            String fileName = this.getFileName("\u041a\u043e\u0440\u043e\u0442\u043a\u0438\u0439 \u043f\u0438\u0441\u043a");
            if (fileName != null) {
                this.blokCyclicPlayer.startCyclicPlay();
                this.blokCyclicPlayer.play(fileName);
            }
        }
    }

    public void stopCyclicBeep() {
        if (this.blokCyclicPlayer.isAlive()) {
            this.blokCyclicPlayer.destructor();
        }
    }

    public void destruct() {
        this.sautSoundPlayer.destructor();
        for (SoundPlayer soundPlayer : this.blokSoundPlayers) {
            soundPlayer.destructor();
        }
    }
}

