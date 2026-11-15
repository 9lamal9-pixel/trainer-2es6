/*
 * Decompiled with CFR 0.152.
 */
package base.data;

import base.data.BLOCK_MODE;
import base.data.DisplayData;
import base.data.LocalData;
import base.data.NetworkData;
import base.data.configs.CommandSettingsData;
import base.data.configs.RoadData;
import base.data.configs.StartConfig;
import base.display.DisplayController;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockData {
    private static final Logger logger = LogManager.getLogger(BlockData.class);
    private static BlockData instance;
    private static final short[] DISTANCE_CAT_1;
    private static final short[] DISTANCE_CAT_2;
    private static final short[] DISTANCE_CAT_3;
    private static final short[] DISTANCE_CAT_4;
    private static final short[] DISTANCE_CAT_5;
    private static final short[] DISTANCE_CAT_6;
    private static final short[] DISTANCE_CAT_7;
    private static final short[] DISTANCE_CAT_8;
    private static final short[] DISTANCE_CAT_TEMP;
    private StartConfig startConfig = new StartConfig();
    private CommandSettingsData commandSettingsData = new CommandSettingsData();
    private RoadData roadData = new RoadData();
    private static final String COMMAND_SETTINGS_DATA_FILE_NAME = "block_command_settings.json";
    private static final String START_CONFIG_FILE_NAME = "start_config.json";
    private NetworkData previousNetworkData = new NetworkData();
    private NetworkData currentNetworkData = new NetworkData();
    private BlockingQueue<NetworkData> currentNetworkDataQueue = new ArrayBlockingQueue<NetworkData>(10);
    private LocalData localData = new LocalData();
    private DisplayData displayData = new DisplayData();
    private DisplayController displayController = null;
    private short[] distanceToTheEndBlockSection = null;

    public static BlockData getInstance() {
        if (instance == null) {
            instance = new BlockData();
        }
        return instance;
    }

    private BlockData() {
        this.startConfig = this.parseConfigFile(this.startConfig, START_CONFIG_FILE_NAME);
        this.commandSettingsData = this.parseConfigFile(this.commandSettingsData, COMMAND_SETTINGS_DATA_FILE_NAME);
        this.reloadRoadData(-1);
        this.commandSettingsData.setWayNumberChange(x -> {
            int way = 0;
            try {
                way = Integer.parseInt(x.toString());
            }
            catch (NumberFormatException e) {
                logger.error(e);
            }
            Object out = String.format("%02d", way);
            out = this.commandSettingsData.getCorrectness_of_way().equals("1") ? (String)out + " \u041f\u0420" : (String)out + " \u041d\u041f";
            this.displayData.setWayNum((String)out);
        });
        this.commandSettingsData.setCorrectnessOfWayChange(x -> {
            int way = 0;
            try {
                way = Integer.parseInt(this.commandSettingsData.getWay_number());
            }
            catch (NumberFormatException e) {
                logger.error(e);
            }
            Object out = String.format("%02d", way);
            out = x.equals("1") ? (String)out + " \u041f\u0420" : (String)out + " \u041d\u041f";
            this.displayData.setWayNum((String)out);
        });
        this.commandSettingsData.setChangeCurrentMode(x -> this.displayData.setTrainMode((String)x));
        this.commandSettingsData.invokeWay();
        if (this.commandSettingsData.getCurrent_mode().equals("\u041f")) {
            this.localData.setBlockMode(BLOCK_MODE.TRAIN);
        } else {
            this.localData.setBlockMode(BLOCK_MODE.MANEUVERING);
        }
        this.commandSettingsData.setTrainCategoryChange(x -> {
            int category = 0;
            try {
                category = Integer.parseInt(x.toString());
            }
            catch (NumberFormatException e) {
                logger.error(e);
            }
            this.distanceToTheEndBlockSection = DISTANCE_CAT_TEMP;
        });
        this.commandSettingsData.invokeTrainCategory();
        logger.debug("init");
    }

    public void removePower() {
        this.writeCommandSettingsDataFile();
        this.previousNetworkData = new NetworkData();
        this.currentNetworkData = new NetworkData();
        this.currentNetworkDataQueue = new ArrayBlockingQueue<NetworkData>(10);
        this.localData = new LocalData();
        this.displayData = new DisplayData();
        logger.debug("device power off");
    }

    public void removeEPK() {
        this.localData = new LocalData();
        this.commandSettingsData.invokeWay();
        if (this.commandSettingsData.getCurrent_mode().equals("\u041f")) {
            this.localData.setBlockMode(BLOCK_MODE.TRAIN);
        } else {
            this.localData.setBlockMode(BLOCK_MODE.MANEUVERING);
        }
    }

    public void reloadRoadData(int roadIndex) {
        if (roadIndex < 0) {
            return;
        }
        String dir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
        File f = new File(dir + File.separator + roadIndex + ".json");
        if (f.exists()) {
            this.roadData = this.parseConfigFile(this.roadData, roadIndex + ".json");
            this.displayController.reinitializeCharts();
        } else {
            this.roadData = this.parseConfigFile(this.roadData, "0.json");
        }
    }

    private <T> T parseConfigFile(T data, String pathToConfigFile) {
        String dir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().replace("%20", " ");
        File configFile = new File(dir + File.separator + pathToConfigFile);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            if (configFile.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader((InputStream)new FileInputStream(configFile), StandardCharsets.UTF_8));
                return gson.fromJson((Reader)reader, (Type)data.getClass());
            }
            this.writeConfigFile(data, pathToConfigFile);
        }
        catch (IOException e) {
            logger.debug(e);
        }
        return data;
    }

    private <T> void writeConfigFile(T data, String pathToConfigFile) {
        String dir = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath()).getParent().replace("%20", " ");
        File configFile = new File(dir + File.separator + pathToConfigFile);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String toWrite = gson.toJson(data);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(configFile, false), StandardCharsets.UTF_8));){
            writer.write(toWrite);
            writer.flush();
        }
        catch (IOException e) {
            logger.debug(e);
        }
    }

    public StartConfig getStartConfig() {
        return this.startConfig;
    }

    public CommandSettingsData getCommandSettingsData() {
        return this.commandSettingsData;
    }

    public void writeCommandSettingsDataFile() {
        this.writeConfigFile(this.commandSettingsData, COMMAND_SETTINGS_DATA_FILE_NAME);
    }

    public BlockingQueue<NetworkData> getCurrentNetworkDataQueue() {
        return this.currentNetworkDataQueue;
    }

    public NetworkData getCurNetworkData() {
        return this.currentNetworkData;
    }

    public NetworkData getPrevNetworkData() {
        return this.previousNetworkData;
    }

    public LocalData getLocalData() {
        return this.localData;
    }

    public DisplayData getDisplayData() {
        return this.displayData;
    }

    public void setDisplayController(DisplayController displayController) {
        if (this.displayController == null) {
            this.displayController = displayController;
        }
    }

    public DisplayController getDisplayController() {
        return this.displayController;
    }

    public short[] getDistanceToTheEndBlockSection() {
        return this.distanceToTheEndBlockSection;
    }

    public RoadData getRoadData() {
        return this.roadData;
    }

    static {
        DISTANCE_CAT_1 = new short[]{0, 5, 9, 14, 19, 24, 28, 33, 39, 44, 49, 54, 60, 66, 71, 77, 83, 89, 95, 102, 108, 115, 121, 128, 135, 143, 150, 157, 165, 173, 180, 189, 197, 205, 214, 222, 231, 240, 250, 259, 269, 278, 288, 298, 309, 319, 330, 341, 352, 363, 374, 386, 398, 410, 422, 435, 447, 460, 473, 486, 500, 513, 527, 541, 556, 570, 585, 600, 615, 630, 646, 661, 677, 694, 710, 727, 743, 761, 778, 795, 813, 831, 849, 868, 886, 905, 924, 943, 963, 983, 1002, 1023, 1043, 1064, 1084, 1106, 1127, 1148, 1170, 1192, 1214, 1237, 1259, 1282, 1305, 1329, 1352, 1376, 1400, 1424, 1449, 1473, 1498, 1523, 1549, 1574, 1600, 1626, 1652, 1679, 1706, 1720, 1734, 1748, 1762, 1777, 1791, 1805, 1819, 1833, 1848, 1862, 1876, 1890, 1905, 1919, 1933, 1947, 1961, 1976, 1990, 2004, 2018, 2032, 2047, 2061, 2075, 2089, 2104, 2118, 2132, 2146, 2160, 2175, 2189, 2203, 2217, 2232, 2246, 2260, 2274, 2288, 2303, 2317, 2331, 2345, 2359, 2374, 2388, 2402, 2416, 2431, 2445, 2459, 2473, 2487, 2502, 2516, 2530, 2544, 2559, 2573, 2587, 2601, 2615, 2630, 2644, 2658, 2672, 2686, 2701, 2715, 2729, 2743, 2758, 2772, 2786, 2800, 2814, 2829, 2843, 2857, 2871, 2885, 2900, 2914, 2928, 2942, 2957, 2971, 2985, 2999, 3013, 3028, 3042, 3056, 3070, 3085, 3099, 3113, 3127, 3141, 3156, 3170, 3184, 3198, 3212, 3227, 3241, 3255, 3269, 3284, 3298, 3312, 3326, 3340, 3355, 3369, 3383, 3397, 3412, 3426, 3440, 3454, 3468, 3483, 3497, 3511, 3525, 3539, 3554};
        DISTANCE_CAT_2 = new short[]{0, 5, 9, 14, 19, 24, 29, 35, 40, 45, 51, 57, 62, 68, 74, 80, 87, 93, 99, 106, 113, 120, 126, 134, 141, 148, 155, 163, 171, 178, 186, 194, 203, 211, 219, 228, 237, 245, 254, 263, 273, 282, 291, 301, 311, 321, 331, 341, 351, 362, 372, 383, 394, 405, 416, 427, 439, 450, 462, 474, 486, 498, 510, 523, 535, 548, 561, 574, 587, 600, 613, 627, 641, 655, 669, 683, 697, 711, 726, 741, 756, 771, 786, 801, 817, 832, 848, 864, 880, 896, 913, 929, 946, 963, 979, 997, 1014, 1031, 1049, 1066, 1084, 1102, 1120, 1139, 1157, 1176, 1194, 1213, 1232, 1251, 1271, 1290, 1310, 1330, 1350, 1370, 1390, 1410, 1431, 1451, 1472, 1493, 1514, 1535, 1557, 1578, 1600, 1622, 1644, 1666, 1688, 1710, 1733, 1756, 1778, 1801, 1824, 1848, 1871, 1895, 1918, 1931, 1945, 1959, 1972, 1986, 2000, 2013, 2027, 2041, 2055, 2068, 2082, 2096, 2109, 2123, 2137, 2150, 2164, 2178, 2192, 2205, 2219, 2233, 2246, 2260, 2274, 2287, 2301, 2315, 2329, 2342, 2356, 2370, 2383, 2397, 2411, 2424, 2438, 2452, 2466, 2479, 2493, 2507, 2520, 2534, 2548, 2561, 2575, 2589, 2603, 2616, 2630, 2644, 2657, 2671, 2685, 2698, 2712, 2726, 2740, 2753, 2767, 2781, 2794, 2808, 2822, 2835, 2849, 2863, 2877, 2890, 2904, 2918, 2931, 2945, 2959, 2972, 2986, 3000, 3014, 3027, 3041, 3055, 3068, 3082, 3096, 3109, 3123, 3137, 3151, 3164, 3178, 3192, 3205, 3219, 3233, 3246, 3260, 3274, 3288, 3301, 3315, 3329, 3342, 3356, 3370, 3383, 3397, 3411, 3425};
        DISTANCE_CAT_3 = new short[]{0, 5, 9, 14, 19, 24, 29, 35, 40, 45, 51, 57, 62, 68, 74, 80, 87, 93, 99, 106, 113, 119, 126, 133, 141, 148, 155, 163, 171, 178, 186, 194, 203, 211, 219, 228, 237, 245, 254, 263, 273, 282, 291, 301, 311, 321, 331, 341, 351, 362, 372, 383, 394, 405, 416, 427, 439, 450, 462, 474, 486, 498, 510, 522, 535, 548, 560, 573, 587, 600, 613, 627, 640, 654, 668, 682, 697, 711, 726, 740, 755, 770, 785, 801, 816, 832, 848, 863, 880, 896, 912, 929, 945, 962, 979, 996, 1013, 1031, 1048, 1066, 1084, 1102, 1120, 1138, 1156, 1175, 1194, 1212, 1231, 1251, 1270, 1289, 1309, 1329, 1348, 1368, 1389, 1409, 1429, 1450, 1471, 1492, 1513, 1534, 1555, 1577, 1598, 1620, 1642, 1664, 1686, 1709, 1731, 1754, 1777, 1800, 1823, 1846, 1869, 1893, 1916, 1940, 1964, 1988, 2012, 2037, 2061, 2086, 2110, 2135, 2160, 2186, 2211, 2236, 2262, 2288, 2314, 2340, 2366, 2392, 2418, 2433, 2448, 2463, 2478, 2493, 2508, 2523, 2538, 2554, 2569, 2584, 2599, 2614, 2629, 2644, 2659, 2674, 2690, 2705, 2720, 2735, 2750, 2765, 2780, 2795, 2810, 2826, 2841, 2856, 2871, 2886, 2901, 2916, 2931, 2946, 2962, 2977, 2992, 3007, 3022, 3037, 3052, 3067, 3082, 3098, 3113, 3128, 3143, 3158, 3173, 3188, 3203, 3218, 3234, 3249, 3264, 3279, 3294, 3309, 3324, 3339, 3354, 3370, 3385, 3400, 3415, 3430, 3445, 3460, 3475, 3490, 3506, 3521, 3536, 3551, 3566, 3581, 3596, 3611, 3627, 3642, 3657, 3672, 3687, 3702, 3717, 3732, 3747, 3763, 3778};
        DISTANCE_CAT_4 = new short[]{0, 4, 8, 12, 16, 20, 24, 29, 33, 38, 43, 47, 52, 57, 63, 68, 73, 79, 84, 90, 96, 102, 108, 114, 121, 127, 134, 140, 147, 154, 161, 168, 176, 183, 191, 199, 206, 214, 223, 231, 239, 248, 256, 265, 274, 283, 292, 302, 311, 321, 330, 340, 350, 360, 371, 381, 392, 402, 413, 424, 435, 447, 458, 470, 481, 493, 505, 517, 529, 542, 554, 567, 580, 593, 606, 619, 633, 646, 660, 674, 688, 702, 716, 731, 745, 760, 775, 790, 805, 821, 836, 852, 867, 883, 899, 916, 932, 948, 965, 982, 999, 1016, 1033, 1050, 1068, 1086, 1103, 1121, 1139, 1158, 1176, 1195, 1213, 1232, 1251, 1270, 1290, 1309, 1329, 1348, 1368, 1388, 1408, 1428, 1449, 1469, 1490, 1511, 1532, 1553, 1574, 1596, 1617, 1639, 1661, 1683, 1705, 1727, 1750, 1772, 1795, 1818, 1841, 1864, 1887, 1910, 1934, 1958, 1981, 2005, 2029, 2054, 2078, 2102, 2127, 2152, 2177, 2202, 2227, 2252, 2278, 2303, 2329, 2355, 2381, 2407, 2433, 2459, 2486, 2512, 2539, 2566, 2593, 2620, 2647, 2675, 2702, 2730, 2757, 2785, 2813, 2841, 2870, 2898, 2927, 2955, 2984, 3013, 3042, 3071, 3100, 3130, 3159, 3189, 3218, 3248, 3278, 3308, 3338, 3369, 3399, 3415, 3432, 3449, 3466, 3483, 3500, 3517, 3534, 3551, 3568, 3585, 3602, 3619, 3636, 3653, 3670, 3687, 3704, 3721, 3738, 3755, 3772, 3789, 3806, 3823, 3840, 3857, 3874, 3891, 3908, 3925, 3942, 3959, 3976, 3993, 4010, 4027, 4044, 4061, 4078, 4095, 4112, 4129, 4146, 4163, 4180, 4197, 4214, 4231, 4248};
        DISTANCE_CAT_5 = new short[]{0, 4, 8, 12, 16, 20, 24, 29, 33, 38, 43, 47, 52, 57, 63, 68, 73, 79, 84, 90, 96, 102, 108, 114, 121, 127, 134, 140, 147, 154, 161, 168, 176, 183, 191, 199, 206, 214, 223, 231, 239, 248, 256, 265, 274, 283, 292, 302, 311, 321, 330, 340, 350, 360, 371, 381, 392, 402, 413, 424, 435, 447, 458, 470, 481, 493, 505, 517, 529, 542, 554, 567, 580, 593, 606, 619, 633, 646, 660, 674, 688, 702, 716, 731, 745, 760, 775, 790, 805, 821, 836, 852, 867, 883, 899, 916, 932, 948, 965, 982, 999, 1016, 1033, 1050, 1068, 1086, 1103, 1121, 1139, 1158, 1176, 1195, 1213, 1232, 1251, 1270, 1290, 1309, 1329, 1348, 1368, 1388, 1408, 1428, 1449, 1469, 1490, 1511, 1532, 1553, 1574, 1596, 1617, 1639, 1661, 1683, 1705, 1727, 1750, 1772, 1795, 1818, 1841, 1864, 1887, 1910, 1934, 1958, 1981, 2005, 2029, 2054, 2078, 2102, 2127, 2152, 2177, 2202, 2227, 2252, 2278, 2303, 2329, 2355, 2381, 2407, 2433, 2459, 2486, 2512, 2539, 2566, 2593, 2620, 2647, 2675, 2702, 2730, 2757, 2785, 2813, 2841, 2870, 2898, 2927, 2955, 2984, 3013, 3042, 3071, 3100, 3130, 3159, 3189, 3218, 3248, 3278, 3308, 3338, 3369, 3399, 3430, 3460, 3491, 3522, 3553, 3584, 3615, 3647, 3678, 3710, 3741, 3773, 3805, 3837, 3869, 3901, 3934, 3966, 3999, 4031, 4064, 4097, 4130, 4163, 4196, 4229, 4263, 4296, 4330, 4364, 4397, 4431, 4465, 4499, 4534, 4568, 4602, 4637, 4671, 4706, 4741, 4776, 4811, 4846, 4881, 4916, 4952, 4987, 5023, 5058};
        DISTANCE_CAT_6 = new short[]{0, 7, 15, 22, 30, 38, 46, 55, 63, 72, 81, 90, 100, 109, 119, 130, 140, 151, 162, 173, 185, 196, 209, 221, 234, 247, 260, 274, 288, 302, 317, 332, 348, 363, 380, 396, 413, 430, 448, 466, 484, 503, 522, 542, 562, 583, 603, 625, 646, 669, 691, 714, 738, 761, 786, 811, 836, 861, 888, 914, 941, 969, 997, 1025, 1054, 1083, 1113, 1144, 1175, 1206, 1238, 1270, 1303, 1336, 1370, 1404, 1439, 1474, 1510, 1546, 1583, 1620, 1658, 1696, 1735, 1774, 1814, 1854, 1895, 1936, 1978, 1999, 2021, 2043, 2065, 2087, 2109, 2131, 2153, 2175, 2197, 2219, 2241, 2263, 2285, 2307, 2329, 2351, 2373, 2395, 2417, 2439, 2461, 2483, 2505, 2527, 2549, 2571, 2593, 2615, 2637, 2659, 2681, 2703, 2725, 2747, 2769, 2791, 2813, 2835, 2857, 2879, 2901, 2923, 2945, 2967, 2988, 3010, 3032, 3054, 3076, 3098, 3120, 3142, 3164, 3186, 3208, 3230, 3252, 3274, 3296, 3318, 3340, 3362, 3384, 3406, 3428, 3450, 3472, 3494, 3516, 3538, 3560, 3582, 3604, 3626, 3648, 3670, 3692, 3714, 3736, 3758, 3780, 3802, 3824, 3846, 3868, 3890, 3912, 3934, 3956, 3977, 3999, 4021, 4043, 4065, 4087, 4109, 4131, 4153, 4175, 4197, 4219, 4241, 4263, 4285, 4307, 4329, 4351, 4373, 4395, 4417, 4439, 4461, 4483, 4505, 4527, 4549, 4571, 4593, 4615, 4637, 4659, 4681, 4703, 4725, 4747, 4769, 4791, 4813, 4835, 4857, 4879, 4901, 4923, 4945, 4966, 4988, 5010, 5032, 5054, 5076, 5098, 5120, 5142, 5164, 5186, 5208, 5230, 5252, 5274, 5296, 5318, 5340, 5362, 5384, 5406, 5428, 5450, 5472, 5494};
        DISTANCE_CAT_7 = new short[]{0, 6, 12, 18, 24, 30, 37, 44, 51, 58, 66, 74, 82, 90, 99, 108, 117, 127, 137, 147, 158, 168, 180, 191, 203, 216, 229, 242, 255, 269, 284, 299, 314, 330, 346, 362, 379, 397, 415, 433, 452, 472, 492, 512, 533, 555, 577, 599, 622, 646, 670, 694, 720, 745, 772, 799, 826, 854, 883, 912, 942, 957, 973, 989, 1004, 1020, 1036, 1051, 1067, 1083, 1099, 1114, 1130, 1146, 1161, 1177, 1193, 1208, 1224, 1240, 1256, 1271, 1287, 1303, 1318, 1334, 1350, 1365, 1381, 1397, 1413, 1428, 1444, 1460, 1475, 1491, 1507, 1522, 1538, 1554, 1570, 1585, 1601, 1617, 1632, 1648, 1664, 1679, 1695, 1711, 1727, 1742, 1758, 1774, 1789, 1805, 1821, 1836, 1852, 1868, 1884, 1899, 1915, 1931, 1946, 1962, 1978, 1993, 2009, 2025, 2041, 2056, 2072, 2088, 2103, 2119, 2135, 2150, 2166, 2182, 2198, 2213, 2229, 2245, 2260, 2276, 2292, 2307, 2323, 2339, 2355, 2370, 2386, 2402, 2417, 2433, 2449, 2464, 2480, 2496, 2512, 2527, 2543, 2559, 2574, 2590, 2606, 2621, 2637, 2653, 2669, 2684, 2700, 2716, 2731, 2747, 2763, 2778, 2794, 2810, 2826, 2841, 2857, 2873, 2888, 2904, 2920, 2935, 2951, 2967, 2983, 2998, 3014, 3030, 3045, 3061, 3077, 3092, 3108, 3124, 3140, 3155, 3171, 3187, 3202, 3218, 3234, 3249, 3265, 3281, 3297, 3312, 3328, 3344, 3359, 3375, 3391, 3406, 3422, 3438, 3454, 3469, 3485, 3501, 3516, 3532, 3548, 3563, 3579, 3595, 3611, 3626, 3642, 3658, 3673, 3689, 3705, 3720, 3736, 3752, 3768, 3783, 3799, 3815, 3830, 3846, 3862, 3877, 3893, 3909, 3925};
        DISTANCE_CAT_8 = new short[]{0, 6, 12, 18, 24, 30, 37, 44, 51, 58, 66, 74, 82, 90, 99, 108, 117, 127, 137, 147, 158, 168, 180, 191, 203, 216, 229, 242, 255, 269, 284, 299, 314, 330, 346, 362, 379, 397, 415, 433, 452};
        DISTANCE_CAT_TEMP = new short[]{0, 15, 32, 48, 65, 82, 99, 119, 136, 156, 175, 195, 216, 236, 257, 281, 303, 326, 350, 374, 400, 411, 424, 436, 449, 462, 475, 489, 503, 517, 532, 547, 563, 578, 595, 611, 628, 645, 663, 681, 699, 718, 737, 757, 777, 798, 818, 840, 861, 884, 906, 929, 953, 976, 1001, 1026, 1051, 1076, 1103, 1129, 1156, 1184, 1212, 1240, 1269, 1298, 1328, 1359, 1390, 1421, 1453, 1485, 1518, 1551, 1585, 1619, 1654, 1689, 1725, 1761, 1798, 1835, 1873, 1911, 1950, 1989, 2029, 2069, 2110, 2151, 2193, 2214, 2236, 2258, 2280, 2302, 2324, 2346, 2368, 2390, 2412, 2434, 2456, 2478, 2500, 2522, 2544, 2566, 2588, 2610, 2632, 2654, 2676, 2698, 2720, 2742, 2764, 2786, 2808, 2830, 2852, 2874, 2896, 2918, 2940, 2962, 2984, 3006, 3028, 3050, 3072, 3094, 3116, 3138, 3160, 3182, 3203, 3225, 3247, 3269, 3291, 3313, 3335, 3357, 3379, 3401, 3423, 3445, 3467, 3489, 3511, 3533, 3555, 3577, 3599, 3621, 3643, 3665, 3687, 3709, 3731, 3753, 3775, 3797, 3819, 3841, 3863, 3885, 3907, 3929, 3951, 3973, 3995, 4017, 4039, 4061, 4083, 4105, 4127, 4149, 4171, 4192, 4214, 4236, 4258, 4280, 4302, 4324, 4346, 4368, 4390, 4412, 4434, 4456, 4478, 4500, 4522, 4544, 4566, 4588, 4610, 4632, 4654, 4676, 4698, 4720, 4742, 4764, 4786, 4808, 4830, 4852, 4874, 4896, 4918, 4940, 4962, 4984, 5006, 5028, 5050, 5072, 5094, 5116, 5138, 5160, 5181, 5203, 5225, 5247, 5269, 5291, 5313, 5335, 5357, 5379, 5401, 5423, 5445, 5467, 5489, 5511, 5533, 5555, 5577, 5599, 5621, 5643, 5665, 5687, 5709};
    }
}

