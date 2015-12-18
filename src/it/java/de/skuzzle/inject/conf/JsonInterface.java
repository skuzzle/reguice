package de.skuzzle.inject.conf;


public interface JsonInterface {
    int getFoo();
    double getPi();
    String getBar();
    boolean isCool();
    int[] getArray();
    SubType getSample();
    Object getUnknown();
    long getWithParameter(Object object);

    public interface SubType {
        Object getObject();
    }
}
