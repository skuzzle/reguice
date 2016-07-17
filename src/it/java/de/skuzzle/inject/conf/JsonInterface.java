package de.skuzzle.inject.conf;

import java.util.List;

public interface JsonInterface {
    int getFoo();

    double getPi();

    String getBar();

    boolean isCool();

    int[] getArray();

    SubType getSample();

    Object getUnknown();

    long getWithParameter(Object object);

    SampleObject getSampleObject();

    List<SubType> getListSample();

    public interface SubType {
        Object getObject();
    }
}
