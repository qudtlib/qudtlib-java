package io.github.qudtlib.vocab;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Constants for the Quantities, Units, Dimensions and Data Types Ontology.
 *
 * <p>TODO: Using hardcoded QUDT.java while waiting for a release of VocGen
 *
 * @see <a href="https://www.qudt.org/">Quantities, Units, Dimensions and Data Types Ontology</a>
 */
public class QUDT {
    /** The QUDT namespace: http://qudt.org/schema/qudt/ */
    public static final String NAMESPACE = "http://qudt.org/schema/qudt/";

    /** Recommended prefix for the namespace: "qudt" */
    public static final String PREFIX = "qudt";

    /** An immutable {@link Namespace} constant that represents the namespace. */
    public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

    // Classes
    /** <code>Quantity Kind (abstract)</code> * */
    public static final IRI AbstractQuantityKind = create("AbstractQuantityKind");

    /** <code>Angle unit</code> * */
    public static final IRI AngleUnit = create("AngleUnit");

    /** <code>Aspect Class</code> * */
    public static final IRI AspectClass = create("AspectClass");

    /** <code>Base Dimension Magnitude</code> * */
    public static final IRI BaseDimensionMagnitude = create("BaseDimensionMagnitude");

    /** <code>Binary Prefix</code> * */
    public static final IRI BinaryPrefix = create("BinaryPrefix");

    /** <code>Bit Encoding</code> * */
    public static final IRI BitEncodingType = create("BitEncodingType");

    /** <code>Boolean encoding type</code> * */
    public static final IRI BooleanEncodingType = create("BooleanEncodingType");

    /** <code>Byte Encoding</code> * */
    public static final IRI ByteEncodingType = create("ByteEncodingType");

    /** <code>Cardinality Type</code> * */
    public static final IRI CardinalityType = create("CardinalityType");

    /** <code>Char Encoding Type</code> * */
    public static final IRI CharEncodingType = create("CharEncodingType");

    /** <code>Citation</code> * */
    public static final IRI Citation = create("Citation");

    /** <code>Comment</code> * */
    public static final IRI Comment = create("Comment");

    /** <code>QUDT Concept</code> * */
    public static final IRI Concept = create("Concept");

    /** <code>Constant value</code> * */
    public static final IRI ConstantValue = create("ConstantValue");

    /** <code>Counting Unit</code> * */
    public static final IRI CountingUnit = create("CountingUnit");

    /** <code>Currency Unit</code> * */
    public static final IRI CurrencyUnit = create("CurrencyUnit");

    /** <code>Data Encoding</code> * */
    public static final IRI DataEncoding = create("DataEncoding");

    /** <code>QUDT Datatype</code> * */
    public static final IRI Datatype = create("Datatype");

    /** <code>Date Time String Encoding Type</code> * */
    public static final IRI DateTimeStringEncodingType = create("DateTimeStringEncodingType");

    /** <code>Decimal Prefix</code> * */
    public static final IRI DecimalPrefix = create("DecimalPrefix");

    /** <code>Derived Unit</code> * */
    public static final IRI DerivedUnit = create("DerivedUnit");

    /** <code>Dimensionless Unit</code> * */
    public static final IRI DimensionlessUnit = create("DimensionlessUnit");

    /** <code>Discipline</code> * */
    public static final IRI Discipline = create("Discipline");

    /** <code>Encoding</code> * */
    public static final IRI Encoding = create("Encoding");

    /** <code>Endian Type</code> * */
    public static final IRI EndianType = create("EndianType");

    /** <code>Enumerated Value</code> * */
    public static final IRI EnumeratedValue = create("EnumeratedValue");

    /** <code>Enumeration</code> * */
    public static final IRI Enumeration = create("Enumeration");

    /** <code>Enumeration scale</code> * */
    public static final IRI EnumerationScale = create("EnumerationScale");

    /** <code>Figure</code> * */
    public static final IRI Figure = create("Figure");

    /** <code>Floating Point Encoding</code> * */
    public static final IRI FloatingPointEncodingType = create("FloatingPointEncodingType");

    /** <code>Integer Encoding</code> * */
    public static final IRI IntegerEncodingType = create("IntegerEncodingType");

    /** <code>Interval scale</code> * */
    public static final IRI IntervalScale = create("IntervalScale");

    /** <code>Logarithmic Unit</code> * */
    public static final IRI LogarithmicUnit = create("LogarithmicUnit");

    /** <code>Maths Function Type</code> * */
    public static final IRI MathsFunctionType = create("MathsFunctionType");

    /** <code>NIST SP~811 Comment</code> * */
    public static final IRI NIST_SP811_Comment = create("NIST_SP811_Comment");

    /** <code>Nominal scale</code> * */
    public static final IRI NominalScale = create("NominalScale");

    /** <code>Ordered type</code> * */
    public static final IRI OrderedType = create("OrderedType");

    /** <code>Ordinal scale</code> * */
    public static final IRI OrdinalScale = create("OrdinalScale");

    /** <code>Organization</code> * */
    public static final IRI Organization = create("Organization");

    /** <code>Physical Constant</code> * */
    public static final IRI PhysicalConstant = create("PhysicalConstant");

    /** <code>Plane Angle Unit</code> * */
    public static final IRI PlaneAngleUnit = create("PlaneAngleUnit");

    /** <code>Prefix</code> * */
    public static final IRI Prefix = create("Prefix");

    /** <code>Quantifiable</code> * */
    public static final IRI Quantifiable = create("Quantifiable");

    /** <code>Quantity</code> * */
    public static final IRI Quantity = create("Quantity");

    /** <code>Quantity Kind</code> * */
    public static final IRI QuantityKind = create("QuantityKind");

    /** <code>Quantity Kind Dimension Vector</code> * */
    public static final IRI QuantityKindDimensionVector = create("QuantityKindDimensionVector");

    /** <code>CGS Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_CGS =
            create("QuantityKindDimensionVector_CGS");

    /** <code>CGS EMU Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_CGS_EMU =
            create("QuantityKindDimensionVector_CGS-EMU");

    /** <code>CGS ESU Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_CGS_ESU =
            create("QuantityKindDimensionVector_CGS-ESU");

    /** <code>CGS GAUSS Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_CGS_GAUSS =
            create("QuantityKindDimensionVector_CGS-GAUSS");

    /** <code>CGS LH Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_CGS_LH =
            create("QuantityKindDimensionVector_CGS-LH");

    /** <code>ISO Dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_ISO =
            create("QuantityKindDimensionVector_ISO");

    /** <code>Imperial dimension vector</code> * */
    public static final IRI QuantityKindDimensionVector_Imperial =
            create("QuantityKindDimensionVector_Imperial");

    /** <code>Quantity Kind Dimension vector (SI)</code> * */
    public static final IRI QuantityKindDimensionVector_SI =
            create("QuantityKindDimensionVector_SI");

    /** <code>Quantity type</code> * */
    public static final IRI QuantityType = create("QuantityType");

    /** <code>Quantity value</code> * */
    public static final IRI QuantityValue = create("QuantityValue");

    /** <code>Ratio scale</code> * */
    public static final IRI RatioScale = create("RatioScale");

    /** <code>Rule</code> * */
    public static final IRI Rule = create("Rule");

    /** <code>Rule Type</code> * */
    public static final IRI RuleType = create("RuleType");

    /** <code>Scalar Datatype</code> * */
    public static final IRI ScalarDatatype = create("ScalarDatatype");

    /** <code>Scale</code> * */
    public static final IRI Scale = create("Scale");

    /** <code>Scale type</code> * */
    public static final IRI ScaleType = create("ScaleType");

    /** <code>Signedness type</code> * */
    public static final IRI SignednessType = create("SignednessType");

    /** <code>Solid Angle Unit</code> * */
    public static final IRI SolidAngleUnit = create("SolidAngleUnit");

    /** <code>Statement</code> * */
    public static final IRI Statement = create("Statement");

    /** <code>String Encoding Type</code> * */
    public static final IRI StringEncodingType = create("StringEncodingType");

    /** <code>Structured Data Type</code> * */
    public static final IRI StructuredDatatype = create("StructuredDatatype");

    /** <code>Symbol</code> * */
    public static final IRI Symbol = create("Symbol");

    /** <code>System of Quantity Kinds</code> * */
    public static final IRI SystemOfQuantityKinds = create("SystemOfQuantityKinds");

    /** <code>System of Units</code> * */
    public static final IRI SystemOfUnits = create("SystemOfUnits");

    /** <code>Transform type</code> * */
    public static final IRI TransformType = create("TransformType");

    /** <code>Unit</code> * */
    public static final IRI Unit = create("Unit");

    /** <code>User Quantity Kind</code> * */
    public static final IRI UserQuantityKind = create("UserQuantityKind");

    // Properties
    /** <code>abbreviation</code> * */
    public static final IRI abbreviation = create("abbreviation");

    /** <code>acronym</code> * */
    public static final IRI acronym = create("acronym");

    /** <code>allowed pattern</code> * */
    public static final IRI allowedPattern = create("allowedPattern");

    /** <code>allowed unit of system</code> * */
    public static final IRI allowedUnitOfSystem = create("allowedUnitOfSystem");

    /** <code>ANSI SQL Name</code> * */
    public static final IRI ansiSQLName = create("ansiSQLName");

    /** <code>applicable CGS unit</code> * */
    public static final IRI applicableCGSUnit = create("applicableCGSUnit");

    /** <code>applicable ISO unit</code> * */
    public static final IRI applicableISOUnit = create("applicableISOUnit");

    /** <code>applicable Imperial unit</code> * */
    public static final IRI applicableImperialUnit = create("applicableImperialUnit");

    /** <code>applicable physical constant</code> * */
    public static final IRI applicablePhysicalConstant = create("applicablePhysicalConstant");

    /** <code>applicable Planck unit</code> * */
    public static final IRI applicablePlanckUnit = create("applicablePlanckUnit");

    /** <code>applicable SI unit</code> * */
    public static final IRI applicableSIUnit = create("applicableSIUnit");

    /** <code>applicable US Customary unit</code> * */
    public static final IRI applicableUSCustomaryUnit = create("applicableUSCustomaryUnit");

    /** <code>applicable unit</code> * */
    public static final IRI applicableUnit = create("applicableUnit");

    /** <code>applicable System of Units for a unit</code> */
    public static final IRI applicableSystem = create("applicableSystem");
    /** /** <code>base CGS unit dimensions</code> * */
    public static final IRI baseCGSUnitDimensions = create("baseCGSUnitDimensions");

    /** <code>base dimension enumeration</code> * */
    public static final IRI baseDimensionEnumeration = create("baseDimensionEnumeration");

    /** <code>base ISO unit dimensions</code> * */
    public static final IRI baseISOUnitDimensions = create("baseISOUnitDimensions");

    /** <code>base Imperial unit dimensions</code> * */
    public static final IRI baseImperialUnitDimensions = create("baseImperialUnitDimensions");

    /** <code>base SI unit dimensions</code> * */
    public static final IRI baseSIUnitDimensions = create("baseSIUnitDimensions");

    /** <code>base US Customary unit dimensions</code> * */
    public static final IRI baseUSCustomaryUnitDimensions = create("baseUSCustomaryUnitDimensions");

    /** <code>base unit dimensions</code> * */
    public static final IRI baseUnitDimensions = create("baseUnitDimensions");

    /** <code>is base unit of system</code> * */
    public static final IRI baseUnitOfSystem = create("baseUnitOfSystem");

    /** <code>basis</code> * */
    public static final IRI basis = create("basis");

    /** <code>belongs to system of quantities</code> * */
    public static final IRI belongsToSystemOfQuantities = create("belongsToSystemOfQuantities");

    /** <code>bit order</code> * */
    public static final IRI bitOrder = create("bitOrder");

    /** <code>bits</code> * */
    public static final IRI bits = create("bits");

    /** <code>bounded</code> * */
    public static final IRI bounded = create("bounded");

    /** <code>byte order</code> * */
    public static final IRI byteOrder = create("byteOrder");

    /** <code>bytes</code> * */
    public static final IRI bytes = create("bytes");

    /** <code>C Language name</code> * */
    public static final IRI cName = create("cName");

    /** <code>cardinality</code> * */
    public static final IRI cardinality = create("cardinality");

    /** <code>categorized as</code> * */
    public static final IRI categorizedAs = create("categorizedAs");

    /** <code>citation</code> * */
    public static final IRI citation = create("citation");

    /** <code>code</code> * */
    @Deprecated public static final IRI code = create("code");

    /** <code>is coherent unit of system</code> * */
    public static final IRI coherentUnitOfSystem = create("coherentUnitOfSystem");

    /** <code>coherent unit system</code> * */
    public static final IRI coherentUnitSystem = create("coherentUnitSystem");

    /** <code>conversion coefficient</code> * */
    public static final IRI conversionCoefficient = create("conversionCoefficient");

    /** <code>conversion multiplier</code> * */
    public static final IRI conversionMultiplier = create("conversionMultiplier");

    /** <code>conversion multiplier</code> * */
    public static final IRI conversionMultiplierSN = create("conversionMultiplierSN");

    /** <code>conversion offset</code> * */
    public static final IRI conversionOffset = create("conversionOffset");

    /** <code>conversion offset</code> * */
    public static final IRI conversionOffsetSN = create("conversionOffsetSN");

    /** <code>currency exponent</code> * */
    public static final IRI currencyExponent = create("currencyExponent");

    /** <code>data encoding</code> * */
    public static final IRI dataEncoding = create("dataEncoding");

    /** <code>data structure</code> * */
    public static final IRI dataStructure = create("dataStructure");

    /** <code>datatype</code> * */
    public static final IRI dataType = create("dataType");

    /** <code>dbpedia match</code> * */
    public static final IRI dbpediaMatch = create("dbpediaMatch");

    /** <code>default</code> * */
    public static final IRI default_ = create("default");

    /** <code>defined unit of system</code> * */
    public static final IRI definedUnitOfSystem = create("definedUnitOfSystem");

    /** <code>denominator dimension vector</code> * */
    public static final IRI denominatorDimensionVector = create("denominatorDimensionVector");

    /** <code>is coherent derived unit of system</code> * */
    public static final IRI derivedCoherentUnitOfSystem = create("derivedCoherentUnitOfSystem");

    /** <code>is non-coherent derived unit of system</code> * */
    public static final IRI derivedNonCoherentUnitOfSystem =
            create("derivedNonCoherentUnitOfSystem");

    /** <code>derived quantity kind of system</code> * */
    public static final IRI derivedQuantityKindOfSystem = create("derivedQuantityKindOfSystem");

    /** <code>is derived unit of system</code> * */
    public static final IRI derivedUnitOfSystem = create("derivedUnitOfSystem");

    /** <code>dimension exponent</code> * */
    public static final IRI dimensionExponent = create("dimensionExponent");

    /** <code>dimension exponent for amount of substance</code> * */
    public static final IRI dimensionExponentForAmountOfSubstance =
            create("dimensionExponentForAmountOfSubstance");

    /** <code>dimension exponent for electric current</code> * */
    public static final IRI dimensionExponentForElectricCurrent =
            create("dimensionExponentForElectricCurrent");

    /** <code>dimension exponent for length</code> * */
    public static final IRI dimensionExponentForLength = create("dimensionExponentForLength");

    /** <code>dimension exponent for luminous intensity</code> * */
    public static final IRI dimensionExponentForLuminousIntensity =
            create("dimensionExponentForLuminousIntensity");

    /** <code>dimension exponent for mass</code> * */
    public static final IRI dimensionExponentForMass = create("dimensionExponentForMass");

    /** <code>dimension exponent for thermodynamic temperature</code> * */
    public static final IRI dimensionExponentForThermodynamicTemperature =
            create("dimensionExponentForThermodynamicTemperature");

    /** <code>dimension exponent for time</code> * */
    public static final IRI dimensionExponentForTime = create("dimensionExponentForTime");

    /** <code>dimension inverse</code> * */
    public static final IRI dimensionInverse = create("dimensionInverse");

    /** <code>dimension vector for SI</code> * */
    public static final IRI dimensionVectorForSI = create("dimensionVectorForSI");

    /** <code>dimensionless exponent</code> * */
    public static final IRI dimensionlessExponent = create("dimensionlessExponent");

    /** <code>element</code> * */
    public static final IRI element = create("element");

    /** <code>element kind</code> * */
    public static final IRI elementKind = create("elementKind");

    /** <code>element type</code> * */
    public static final IRI elementType = create("elementType");

    /** <code>encoding</code> * */
    public static final IRI encoding = create("encoding");

    /** <code>exact constant</code> * */
    public static final IRI exactConstant = create("exactConstant");

    /** <code>exact match</code> * */
    public static final IRI exactMatch = create("exactMatch");

    /** <code>field code</code> * */
    public static final IRI fieldCode = create("fieldCode");

    /** <code>figure</code> * */
    public static final IRI figure = create("figure");

    /** <code>figure caption</code> * */
    public static final IRI figureCaption = create("figureCaption");

    /** <code>figure label</code> * */
    public static final IRI figureLabel = create("figureLabel");

    /** <code>generalization</code> * */
    public static final IRI generalization = create("generalization");

    /** <code>guidance</code> * */
    public static final IRI guidance = create("guidance");

    /** <code>allowed unit</code> * */
    public static final IRI hasAllowedUnit = create("hasAllowedUnit");

    /** <code>has base quantity kind</code> * */
    public static final IRI hasBaseQuantityKind = create("hasBaseQuantityKind");

    /** <code>base unit</code> * */
    public static final IRI hasBaseUnit = create("hasBaseUnit");

    /** <code>coherent unit</code> * */
    public static final IRI hasCoherentUnit = create("hasCoherentUnit");

    /** <code>defined unit</code> * */
    public static final IRI hasDefinedUnit = create("hasDefinedUnit");

    /** <code>has quantity kind dimension vector denominator part</code> * */
    public static final IRI hasDenominatorPart = create("hasDenominatorPart");

    /** <code>derived coherent unit</code> * */
    public static final IRI hasDerivedCoherentUnit = create("hasDerivedCoherentUnit");

    /** <code>has coherent derived unit</code> * */
    public static final IRI hasDerivedNonCoherentUnit = create("hasDerivedNonCoherentUnit");

    /** <code>derived unit</code> * */
    public static final IRI hasDerivedUnit = create("hasDerivedUnit");

    /** <code>has dimension</code> * */
    public static final IRI hasDimension = create("hasDimension");

    /** <code>dimension expression</code> * */
    public static final IRI hasDimensionExpression = create("hasDimensionExpression");

    /** <code>has dimension vector</code> * */
    public static final IRI hasDimensionVector = create("hasDimensionVector");

    /** <code>has non-coherent unit</code> * */
    public static final IRI hasNonCoherentUnit = create("hasNonCoherentUnit");

    /** <code>has quantity kind dimension vector numerator part</code> * */
    public static final IRI hasNumeratorPart = create("hasNumeratorPart");

    /** <code>prefix unit</code> * */
    public static final IRI hasPrefixUnit = create("hasPrefixUnit");

    /** <code>has quantity</code> * */
    public static final IRI hasQuantity = create("hasQuantity");

    /** <code>has quantity kind</code> * */
    public static final IRI hasQuantityKind = create("hasQuantityKind");

    /** <code>has reference quantity kind</code> * */
    public static final IRI hasReferenceQuantityKind = create("hasReferenceQuantityKind");

    /** <code>has rule</code> * */
    public static final IRI hasRule = create("hasRule");

    /** <code>has unit</code> * */
    public static final IRI hasUnit = create("hasUnit");

    /** <code>has unit system</code> * */
    public static final IRI hasUnitSystem = create("hasUnitSystem");

    /** <code>has vocabulary</code> * */
    public static final IRI hasVocabulary = create("hasVocabulary");

    /** <code>height</code> * */
    public static final IRI height = create("height");

    /** <code>qudt id</code> * */
    public static final IRI id = create("id");

    /** <code>iec-61360 code</code> * */
    public static final IRI iec61360Code = create("iec61360Code");

    /** <code>image</code> * */
    public static final IRI image = create("image");

    /** <code>image location</code> * */
    public static final IRI imageLocation = create("imageLocation");

    /** <code>informative reference</code> * */
    public static final IRI informativeReference = create("informativeReference");

    /** <code>is base quantity kind of system</code> * */
    public static final IRI isBaseQuantityKindOfSystem = create("isBaseQuantityKindOfSystem");

    /** <code>is Delta Quantity</code> * */
    public static final IRI isDeltaQuantity = create("isDeltaQuantity");

    /** <code>is dimension in system</code> * */
    public static final IRI isDimensionInSystem = create("isDimensionInSystem");

    /** <code>is metric unit</code> * */
    public static final IRI isMetricUnit = create("isMetricUnit");

    /** <code>is quantity kind of</code> * */
    public static final IRI isQuantityKindOf = create("isQuantityKindOf");

    /** <code>is scaling of</code> * */
    public static final IRI isScalingOf = create("isScalingOf");

    /** <code>normative reference (ISO)</code> * */
    public static final IRI isoNormativeReference = create("isoNormativeReference");

    /** <code>java name</code> * */
    public static final IRI javaName = create("javaName");

    /** <code>Javascript name</code> * */
    public static final IRI jsName = create("jsName");

    /** <code>landscape</code> * */
    public static final IRI landscape = create("landscape");

    /** <code>latex definition</code> * */
    public static final IRI latexDefinition = create("latexDefinition");

    /** <code>latex symbol</code> * */
    public static final IRI latexSymbol = create("latexSymbol");

    /** <code>length</code> * */
    public static final IRI length = create("length");

    /** <code>literal</code> * */
    public static final IRI literal = create("literal");

    /** <code>lower bound</code> * */
    public static final IRI lowerBound = create("lowerBound");

    /** <code>math definition</code> * */
    public static final IRI mathDefinition = create("mathDefinition");

    /** <code>mathML definition</code> * */
    public static final IRI mathMLdefinition = create("mathMLdefinition");

    /** <code>matlab name</code> * */
    public static final IRI matlabName = create("matlabName");

    /** <code>max exclusive</code> * */
    public static final IRI maxExclusive = create("maxExclusive");

    /** <code>max inclusive</code> * */
    public static final IRI maxInclusive = create("maxInclusive");

    /** <code>Microsoft SQL Server name</code> * */
    public static final IRI microsoftSQLServerName = create("microsoftSQLServerName");

    /** <code>min exclusive</code> * */
    public static final IRI minExclusive = create("minExclusive");

    /** <code>min inclusive</code> * */
    public static final IRI minInclusive = create("minInclusive");

    /** <code>MySQL name</code> * */
    public static final IRI mySQLName = create("mySQLName");

    /** <code>negative delta limit</code> * */
    public static final IRI negativeDeltaLimit = create("negativeDeltaLimit");

    /** <code>normative reference</code> * */
    public static final IRI normativeReference = create("normativeReference");

    /** <code>numerator dimension vector</code> * */
    public static final IRI numeratorDimensionVector = create("numeratorDimensionVector");

    /** <code>numeric value</code> * */
    public static final IRI numericValue = create("numericValue");

    /** <code>ODBC name</code> * */
    public static final IRI odbcName = create("odbcName");

    /** <code>OLE DB name</code> * */
    public static final IRI oleDBName = create("oleDBName");

    /** <code>om unit</code> * */
    public static final IRI omUnit = create("omUnit");

    /** <code>online reference</code> * */
    public static final IRI onlineReference = create("onlineReference");

    /** <code>ORACLE SQL name</code> * */
    public static final IRI oracleSQLName = create("oracleSQLName");

    /** <code>order</code> * */
    public static final IRI order = create("order");

    /** <code>ordered type</code> * */
    public static final IRI orderedType = create("orderedType");

    /** <code>out of scope</code> * */
    public static final IRI outOfScope = create("outOfScope");

    /** <code>permissible maths</code> * */
    public static final IRI permissibleMaths = create("permissibleMaths");

    /** <code>permissible transformation</code> * */
    public static final IRI permissibleTransformation = create("permissibleTransformation");

    /** <code>description (plain text)</code> * */
    public static final IRI plainTextDescription = create("plainTextDescription");

    /** <code>Positive delta limit</code> * */
    public static final IRI positiveDeltaLimit = create("positiveDeltaLimit");

    /** <code>prefix</code> * */
    public static final IRI prefix = create("prefix");

    /** <code>prefix multiplier</code> * */
    public static final IRI prefixMultiplier = create("prefixMultiplier");

    /** <code>protocol buffers name</code> * */
    public static final IRI protocolBuffersName = create("protocolBuffersName");

    /** <code>python name</code> * */
    public static final IRI pythonName = create("pythonName");

    /** <code>denominator dimension vector</code> * */
    public static final IRI qkdvDenominator = create("qkdvDenominator");

    /** <code>numerator dimension vector</code> * */
    public static final IRI qkdvNumerator = create("qkdvNumerator");

    /** <code>quantity</code> * */
    public static final IRI quantity = create("quantity");

    /** <code>quantity value</code> * */
    public static final IRI quantityValue = create("quantityValue");

    /** <code>rationale</code> * */
    public static final IRI rationale = create("rationale");

    /** <code>rdfs datatype</code> * */
    public static final IRI rdfsDatatype = create("rdfsDatatype");

    /** <code>reference</code> * */
    public static final IRI reference = create("reference");

    /** <code>reference unit</code> * */
    public static final IRI referenceUnit = create("referenceUnit");

    /** <code>relative standard uncertainty</code> * */
    public static final IRI relativeStandardUncertainty = create("relativeStandardUncertainty");

    /** <code>relevant quantity kind</code> * */
    public static final IRI relevantQuantityKind = create("relevantQuantityKind");

    /** <code>Relevant Unit</code> * */
    public static final IRI relevantUnit = create("relevantUnit");

    /** <code>rule type</code> * */
    public static final IRI ruleType = create("ruleType");

    /** <code>scale type</code> * */
    public static final IRI scaleType = create("scaleType");

    /** <code>si units expression</code> * */
    public static final IRI siUnitsExpression = create("siUnitsExpression");

    /** <code>specialization</code> * */
    public static final IRI specialization = create("specialization");

    /** <code>standard uncertainty</code> * */
    public static final IRI standardUncertainty = create("standardUncertainty");

    /** <code>standard uncertainty</code> * */
    public static final IRI standardUncertaintySN = create("standardUncertaintySN");

    /** <code>symbol</code> * */
    public static final IRI symbol = create("symbol");

    /** <code>system definition</code> * */
    public static final IRI systemDefinition = create("systemDefinition");

    /** <code>system derived quantity kind</code> * */
    public static final IRI systemDerivedQuantityKind = create("systemDerivedQuantityKind");

    /** <code>system dimension</code> * */
    public static final IRI systemDimension = create("systemDimension");

    /** <code>ucum case-insensitive code</code> * */
    @Deprecated public static final IRI ucumCaseInsensitiveCode = create("ucumCaseInsensitiveCode");

    /** <code>ucum case-sensitive code</code> * */
    @Deprecated public static final IRI ucumCaseSensitiveCode = create("ucumCaseSensitiveCode");

    /** <code>ucum code</code> * */
    public static final IRI ucumCode = create("ucumCode");

    /** <code>udunits code</code> * */
    public static final IRI udunitsCode = create("udunitsCode");

    /** <code>unece common code</code> * */
    public static final IRI uneceCommonCode = create("uneceCommonCode");

    /** <code>unit</code> * */
    public static final IRI unit = create("unit");

    /** <code>unit for</code> * */
    public static final IRI unitFor = create("unitFor");

    /** <code>is unit of system</code> * */
    public static final IRI unitOfSystem = create("unitOfSystem");

    /** <code>upper bound</code> * */
    public static final IRI upperBound = create("upperBound");

    /** <code>url</code> * */
    public static final IRI url = create("url");

    /** <code>value</code> * */
    public static final IRI value = create("value");

    /** <code>value</code> * */
    public static final IRI valueSN = create("valueSN");

    /** <code>value for quantity</code> * */
    public static final IRI valueQuantity = create("valueQuantity");

    /** <code>Vusal Basic name</code> * */
    public static final IRI vbName = create("vbName");

    /** <code>vector magnitude</code> * */
    public static final IRI vectorMagnitude = create("vectorMagnitude");

    /** <code>width</code> * */
    public static final IRI width = create("width");

    // Individuals
    /** <code>QUDT Aspect</code> * */
    public static final IRI Aspect = create("Aspect");

    /** <code>Big Endian</code> * */
    public static final IRI BigEndian = create("BigEndian");

    /** <code>Bit Encoding</code> * */
    public static final IRI BitEncoding = create("BitEncoding");

    /** <code>Boolean Encoding</code> * */
    public static final IRI BooleanEncoding = create("BooleanEncoding");

    /** <code>Countably Infinite Cardinality Type</code> * */
    public static final IRI CT_COUNTABLY_INFINITE = create("CT_COUNTABLY-INFINITE");

    /** <code>Finite Cardinality Type</code> * */
    public static final IRI CT_FINITE = create("CT_FINITE");

    /** <code>Uncountable Cardinality Type</code> * */
    public static final IRI CT_UNCOUNTABLE = create("CT_UNCOUNTABLE");

    /** <code>Char Encoding</code> * */
    public static final IRI CharEncoding = create("CharEncoding");

    /** <code>Single Precision Real Encoding</code> * */
    public static final IRI DoublePrecisionEncoding = create("DoublePrecisionEncoding");

    /** <code>IEEE 754 1985 Real Encoding</code> * */
    public static final IRI IEEE754_1985RealEncoding = create("IEEE754_1985RealEncoding");

    /** <code>ISO 8601 UTC Date Time - Basic Format</code> * */
    public static final IRI ISO8601_UTCDateTime_BasicFormat =
            create("ISO8601-UTCDateTime-BasicFormat");

    /** <code>Little Endian</code> * */
    public static final IRI LittleEndian = create("LittleEndian");

    /** <code>Long Unsigned Integer Encoding</code> * */
    public static final IRI LongUnsignedIntegerEncoding = create("LongUnsignedIntegerEncoding");

    /** <code>OCTET Encoding</code> * */
    public static final IRI OctetEncoding = create("OctetEncoding");

    /** <code>Partially Ordered</code> * */
    public static final IRI PartiallyOrdered = create("PartiallyOrdered");

    /** <code>Signed</code> * */
    public static final IRI SIGNED = create("SIGNED");

    /** <code>Short Signed Integer Encoding</code> * */
    public static final IRI ShortSignedIntegerEncoding = create("ShortSignedIntegerEncoding");

    /** <code>Short Unsigned Integer Encoding</code> * */
    public static final IRI ShortUnsignedIntegerEncoding = create("ShortUnsignedIntegerEncoding");

    /** <code>Signed Integer Encoding</code> * */
    public static final IRI SignedIntegerEncoding = create("SignedIntegerEncoding");

    /** <code>Single Precision Real Encoding</code> * */
    public static final IRI SinglePrecisionRealEncoding = create("SinglePrecisionRealEncoding");

    /** <code>Totally Ordered</code> * */
    public static final IRI TotallyOrdered = create("TotallyOrdered");

    /** <code>Unsigned</code> * */
    public static final IRI UNSIGNED = create("UNSIGNED");

    /** <code>UTF-16 String</code> * */
    public static final IRI UTF16_StringEncoding = create("UTF16-StringEncoding");

    /** <code>UTF-8 Encoding</code> * */
    public static final IRI UTF8_StringEncoding = create("UTF8-StringEncoding");

    /** <code>Unordered</code> * */
    public static final IRI Unordered = create("Unordered");

    /** <code>Unsigned Integer Encoding</code> * */
    public static final IRI UnsignedIntegerEncoding = create("UnsignedIntegerEncoding");

    /** <code>Verifiable</code> * */
    public static final IRI Verifiable = create("Verifiable");

    /** <code>Wikipedia</code> * */
    public static final IRI Wikipedia = create("Wikipedia");

    public static final IRI expression = create("expression");
    public static IRI LatexString = create("LatexString");

    public static IRI UCUMcs = create("UCUMcs");

    private static IRI create(String localName) {
        return SimpleValueFactory.getInstance().createIRI(QUDT.NAMESPACE, localName);
    }
}
