// package org.movsim.consumption.input.xml.batch;
//
// import java.util.Map;
//
// import org.jdom.Element;
// import org.movsim.consumption.input.xml.XmlElementNames;
// import org.movsim.utilities.XmlUtils;
//
// import com.google.common.base.Preconditions;
//
// public class BatchDataInput {
//
// private final String inputFile;
// private final String outputFile;
// private final String modelLabel;
//
// private final ColumnInput columnData;
// private final ConversionInput conversionInput;
//
// public BatchDataInput(Element element) {
// Preconditions.checkNotNull(element);
// Map<String, String> batchInputDataMap = XmlUtils.putAttributesInHash(element);
// this.inputFile = batchInputDataMap.get("inputfile");
// this.outputFile = batchInputDataMap.get("outputfile");
// this.modelLabel = batchInputDataMap.get("model");
// this.columnData = new ColumnInput(element.getChild(XmlElementNames.ColumnDataElement));
// this.conversionInput = new ConversionInput(element.getChild(XmlElementNames.ConversionDataElement));
// }
//
// /**
// * @return the columnData
// */
// public ColumnInput getColumnData() {
// return columnData;
// }
//
// /**
// * @return the conversionInput
// */
// public ConversionInput getConversionInput() {
// return conversionInput;
// }
//
// /**
// * @return the inputFile
// */
// public String getInputFile() {
// return inputFile;
// }
//
// public String getOutputFile() {
// return outputFile;
// }
//
// public String getModelLabel() {
// return modelLabel;
// }
//
// }
