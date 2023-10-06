package net.dumbcode.projectnublar.server.fossil.base;

import com.google.common.collect.Range;
import com.mojang.datafixers.util.Pair;
import net.dumbcode.projectnublar.server.fossil.StoneTypeHandler;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.math.Mth.clamp;

//TODO: HAVE ROCKS AND FOSSILS IDENTIFY TIME PERIODS TO USE FOR SPAWNING
//TODO: add deviation stuff
//TODO: rock strata
public enum DinosaurAge {
    QUATERNARY_HOLOCENE(0.0117, 0),
    QUATERNARY_PLEISTOCENE(2.58, 0.0117),
    NEOGENE_PLIOCENE(5.333, 2.58),
    NEOGENE_MIOCENE(23.03, 5.333),
    PALEOGENE_OLIGOCENE(33.9, 23.03),
    PALEOGENE_EOCENE(56.0, 33.9),
    PALEOGENE_PALEOCENE(66.0, 56.0),
    CRETACEOUS_UPPER(100.5, 66.0),
    CRETACEOUS_LOWER(145, 100.5),
    JURASSIC_UPPER(161.5 /*+-1*/, 145),
    JURASSIC_MIDDLE(174.7 /*+-0.8*/, 161.5 /*+-1*/),
    JURASSIC_LOWER(201.4 /*+-0.2*/, 174.7 /*+-0.8*/),
    TRIASSIC_UPPER(237, 201.4 /*+-0.2*/),
    TRIASSIC_MIDDLE(247.2, 237),
    TRIASSIC_LOWER(251.902 /*+-0.024*/, 247.2),
    PERMIAN_LOPINGIAN(259.51 /*0.21*/, 251.902 /*+-0.024*/),
    PERMIAN_GUADALUPIAN(273.01 /*+-0.14*/, 259.51  /*+-0.21*/),
    PERMIAN_CISURALIAN(298.9  /*+-0.15*/, 273.01 /*+-0.14*/),
    CARBONIFEROUS_PENNSYLVANIAN_UPPER(307  /*+-0.1*/, 298.9 /*+-0.15*/),
    CARBONIFEROUS_PENNSYLVANIAN_MIDDLE(315.2 /*+-0.2*/, 307  /*+-0.1*/),
    CARBONIFEROUS_PENNSYLVANIAN_LOWER(323.2 /*+-0.4*/, 215.2 /*+-0.2*/),
    CARBONIFEROUS_MISSISSIPPIAN_UPPER(330.9  /*+-0.2*/, 323.2  /*+-0.4*/),
    CARBONIFEROUS_MISSISSIPPIAN_MIDDLE(346.7 /*+-0.4*/, 330.9 /*+-0.2*/),
    CARBONIFEROUS_MISSISSIPPIAN_LOWER(358.9  /*+-0.4*/, 346.7 /*+-0.4*/),
    DEVONIAN_UPPER(382.7 /*+-1.6*/, 358.9 /*+-0.4*/),
    DEVONIAN_MIDDLE(393.3 /*+-1.2*/, 382.7 /*+-1.6*/),
    DEVONIAN_LOWER(419.2 /*+-3.2*/, 393.3 /*+-1.2*/),
    SILURIAN_PRIDOLI(423 /*+-2.3*/, 419.2 /*+-3.2*/),
    SILURIAN_LUDLOW(427.4 /*+-0.5*/, 423 /*+-2.3*/),
    SILURIAN_WENLOCK(433.4 /*+-0.8*/, 427.4 /*+-0.5*/),
    SILURIAN_LLANDOVERY(443.8 /*+-1.5*/, 433.4 /*+-0.8*/),
    ORDOVICIAN_UPPER(458.4 /*+-0.9*/, 443.8 /*+-1.5*/),
    ORDOVICIAN_MIDDLE(470 /*+-1.4*/, 458.4 /*+-0.9*/),
    ORDOVICIAN_LOWER(485.4 /*+-1.9*/, 470 /*+-1.4*/),
    CAMBRIAN_FURONGIAN(497, 485.4 /*+-1.9*/),
    CAMBRIAN_MIAOLINGIAN(509, 497),
    CAMBRIAN_SERIES_2(521, 509),
    CAMBRIAN_TERRENEUVIAN(538.8 /*+-0.2*/, 521),
    PROTEROZOIC_NEOPROTEROZOIC_EDIACARAN(635, 538 /*+-0.2*/),
    PROTEROZOIC_NEOPROTEROZOIC_CRYOGENIAN(720, 635),
    PROTEROZOIC_NEOPROTEROZOIC_TONIAN(1000, 720),
    PROTEROZOIC_MESOPROTER0ZOIC_STENIAN(1200, 1000),
    PROTEROZOIC_MESOPROTER0ZOIC_ECTASIAN(1400, 1200),
    PROTEROZOIC_MESOPROTER0ZOIC_CALYMMIAN(1600, 1400),
    PROTEROZOIC_PALEOPROTEROZOIC_STATHERIAN(1800, 1600),
    PROTEROZOIC_PALEOPROTEROZOIC_OROSIRIAN(2050, 1800),
    PROTEROZOIC_PALEOPROTEROZOIC_RHYACIAN(2300, 2050),
    PROTEROZOIC_PALEOPROTEROZOIC_SIDERIAN(2500, 2300),
    ARCHEAN_NEOARCHEAN(2800, 2500),
    ARCHEAN_MESOARCHEAN(3200, 2800),
    ARCHEAN_PALEOARCHEAN(3600, 3200),
    ARCHEAN_EOARCHEAN(4000, 3600),
    HADEAN(4567, 4000);


    private final double start;
    private final double end;

    DinosaurAge(double start, double end) {
        if (end > start) {
            throw new IllegalArgumentException("End time cannot be greater than start");
        }
        this.start = start;
        this.end = end;
    }

    DinosaurAge() {
        this.start = 0;
        this.end = 0;
    }

    public Pair<Double, Double> getTimeSpan() {
        return new Pair<>(start, end);
    }

    public static List<DinosaurAge> findAllTimePeriodsThatMatchRange(double start, double end) {
        List<DinosaurAge> dinosaurAges = new ArrayList<>();
        for (DinosaurAge dinosaurAge : values()) {
            if (Range.closed(end, start).contains(dinosaurAge.start) || Range.closed(end, start).contains(dinosaurAge.end)) {
                dinosaurAges.add(dinosaurAge);
            }
        }
        return dinosaurAges;
    }

    public static List<StoneType> findAllStoneTypesThatMatchRange(double start, double end) {
        List<StoneType> stoneTypes = new ArrayList<>();
        for (StoneType type : StoneTypeHandler.STONE_TYPE_REGISTRY.get()) {
            if (Range.closed(end, start).contains(type.start) || Range.closed(end, start).contains(type.end)) {
                stoneTypes.add(type);
            }
        }
        return stoneTypes;
    }

    public static List<StoneType> findAllStoneTypesThatMatchTimePeriods(List<DinosaurAge> dinosaurAges) {
        List<StoneType> stoneTypes = new ArrayList<>();
        for (DinosaurAge dinosaurAge : dinosaurAges) {
            for (StoneType type : StoneTypeHandler.STONE_TYPE_REGISTRY.get()) {
                if (Range.closed(dinosaurAge.end, dinosaurAge.start).contains(type.start) || Range.closed(dinosaurAge.end, dinosaurAge.start).contains(type.end)) {
                    stoneTypes.add(type);
                }
            }
        }
        return stoneTypes;
    }

    public static Range<Integer> getYLevelsFromTime(int max, double timeStart, double timeEnd) {
        int y = normalize(timeEnd, 4567, 0, 0, max);
        int y1 = normalize(timeStart, 4567, 0, 0, max);
        return Range.closed(y, y1);
    }

    private static int normalize(double value, int minValue, int maxValue, int min, int max) {
        return (int) ((value - minValue) / (minValue - maxValue) * (max - min) + min);
    }
}