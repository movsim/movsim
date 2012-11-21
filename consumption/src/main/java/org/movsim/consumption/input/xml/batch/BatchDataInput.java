package org.movsim.consumption.input.xml.batch;

import java.util.Map;

import org.jdom.Element;
import org.movsim.consumption.input.xml.XmlElementNames;
import org.movsim.utilities.XmlUtils;

public class BatchDataInput extends BatchInput {

    private final String file;

    private final ColumnInput columnData;
    private final ConversionInput conversionInput;

    public BatchDataInput(Element element) {
        Map<String, String> batchInputDataMap = XmlUtils.putAttributesInHash(element);
        this.file = batchInputDataMap.get("file");
        this.columnData = new ColumnInput(element.getChild(XmlElementNames.ColumnDataElement));
        this.conversionInput = new ConversionInput(element.getChild(XmlElementNames.ColumnDataElement));
    }

    /**
     * @return the columnData
     */
    public ColumnInput getColumnData() {
        return columnData;
    }

    /**
     * @return the conversionInput
     */
    public ConversionInput getConversionInput() {
        return conversionInput;
    }

    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

}
