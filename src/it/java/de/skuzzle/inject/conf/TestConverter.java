package de.skuzzle.inject.conf;

public class TestConverter implements Converter<SampleObject> {

    @Override
    public Class<SampleObject> forType() {
        return SampleObject.class;
    }

    @Override
    public SampleObject parseString(String s) throws ConverterException {
        return new SampleObject(s.split(","));
    }

}
