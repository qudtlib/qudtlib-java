# Executable QUDT Library

Provides unit conversion and related functionality for Java.

Makes all conversions and related functionality defined by the excellent [QUDT ontology](https://qudt.org) available in a
**self-contained jar**  with **no external dependencies**. The jar containing all dependencies is **~400kB large**.

The library offers
* 1745 units, such as second, Fahrenheit, or light year
* 881 quantityKinds, such as width, pressure ratio or currency
* 29 prefixes, such as mega, kibi, or atto

...all of which the library converts if possible.

## Usage

The module clients will want to use is `io.github.qudtlib:qudtlib`. The other modules are merely needed to generate that one from the latest [QUDT release zip](https://github.com/qudt/qudt-public-repo/tags).

Maven dependency:
```
<dependency>
	<groupId>io.github.qudtlib</groupId>
	<artifactId>qudtlib</artifactId>
	<version>${project.version}</version> <!-- remember to use the latest version -->
</dependency>
```
## Examples
(see also module qudtlib-example)

### Unit conversion
Converting 38.5° Celsius into Fahrenheit:
```
	Qudt.convert(new BigDecimal("38.5"), Qudt.Units.DEG_C, Qudt.Units.DEG_F));
	-->
	101.3003929999999551224000000000036unit:DEG_F
```
### Find unit by factor units
Finding unit for factors: m, kg, and s^-2:
```
	Set<Unit> myUnits =
			Qudt.derivedUnit(
					Qudt.Units.M, 1,
					Qudt.Units.KiloGM, 1,
					Qudt.Units.SEC, -2);
	-->
	N
```
### Find factor units of a derived unit:
Finding factors of unit N:
```
	List<FactorUnit> myFactorUnits = Qudt.Units.N.getFactorUnits();
	-->
	FU{m^1}
	FU{kg^1}
	FU{s^-2}
```
### Scale Conversion
Converting 1N into kN:
```
	QuantityValue quantityValue = new QuantityValue(new BigDecimal("1"), Qudt.Units.N);
	QuantityValue converted = Qudt.convert(quantityValue, Qudt.Units.KiloN);
	-->
	0.001kN
```
### List convertible units
Which units can we convert to from L?
```
	Unit fromUnit = Qudt.Units.L;
	for (Unit unit : Qudt.allUnits()) {
		if (Qudt.isConvertible(fromUnit, unit)) {
			System.out.println("  " + unit + " (" + unit.getIri() + ")");
		}
	}
	-->
	unit:GT (http://qudt.org/vocab/unit/GT)
	tsp (http://qudt.org/vocab/unit/TSP)
	cup (http://qudt.org/vocab/unit/CUP)
	unit:YD3 (http://qudt.org/vocab/unit/YD3)
	RT (http://qudt.org/vocab/unit/RT)
	unit:M3 (http://qudt.org/vocab/unit/M3)
	fL (http://qudt.org/vocab/unit/FemtoL)
	dry_pt (http://qudt.org/vocab/unit/PINT_US_DRY)
	daL (http://qudt.org/vocab/unit/DecaL)
	pL (http://qudt.org/vocab/unit/PicoL)
	unit:CUP_US (http://qudt.org/vocab/unit/CUP_US)
	hL (http://qudt.org/vocab/unit/HectoL)
	ML (http://qudt.org/vocab/unit/MegaL)
	unit:BBL_US (http://qudt.org/vocab/unit/BBL_US)
	st (http://qudt.org/vocab/unit/STR)
	oz (http://qudt.org/vocab/unit/OZ_VOL_US)
	unit:OZ_VOL_UK (http://qudt.org/vocab/unit/OZ_VOL_UK)
	dL (http://qudt.org/vocab/unit/DeciL)
	dry_gal (http://qudt.org/vocab/unit/GAL_US_DRY)
	tbsp (http://qudt.org/vocab/unit/TBSP)
	unit:Standard (http://qudt.org/vocab/unit/Standard)
	nL (http://qudt.org/vocab/unit/NanoL)
	unit:MilliM3 (http://qudt.org/vocab/unit/MilliM3)
	gal (http://qudt.org/vocab/unit/GAL_IMP)
	qt (http://qudt.org/vocab/unit/QT_US)
	unit:PlanckVolume (http://qudt.org/vocab/unit/PlanckVolume)
	pk (http://qudt.org/vocab/unit/PK_US_DRY)
	unit:PK_UK (http://qudt.org/vocab/unit/PK_UK)
	mL (http://qudt.org/vocab/unit/MilliL)
	μL (http://qudt.org/vocab/unit/MicroL)
	unit:DeciM3 (http://qudt.org/vocab/unit/DeciM3)
	pt (http://qudt.org/vocab/unit/PINT_US)
	unit:PINT_UK (http://qudt.org/vocab/unit/PINT_UK)
	Bf (http://qudt.org/vocab/unit/FBM)
	pi (http://qudt.org/vocab/unit/PINT)
	unit:GAL_UK (http://qudt.org/vocab/unit/GAL_UK)
	gal (http://qudt.org/vocab/unit/GAL_US)
	bbl (http://qudt.org/vocab/unit/BBL)
	unit:MicroM3 (http://qudt.org/vocab/unit/MicroM3)
	ac-ft (http://qudt.org/vocab/unit/AC-FT)
	unit:FT3 (http://qudt.org/vocab/unit/FT3)
	unit:QT_UK (http://qudt.org/vocab/unit/QT_UK)
	unit:CentiM3 (http://qudt.org/vocab/unit/CentiM3)
	bui (http://qudt.org/vocab/unit/BU_UK)
	bua (http://qudt.org/vocab/unit/BU_US)
	unit:MI3 (http://qudt.org/vocab/unit/MI3)
	unit:BBL_US_DRY (http://qudt.org/vocab/unit/BBL_US_DRY)
	kL (http://qudt.org/vocab/unit/KiloL)
	unit:BBL_UK_PET (http://qudt.org/vocab/unit/BBL_UK_PET)
	unit:IN3 (http://qudt.org/vocab/unit/IN3)
	dry_qt (http://qudt.org/vocab/unit/QT_US_DRY)
	L (http://qudt.org/vocab/unit/L)
	cL (http://qudt.org/vocab/unit/CentiL)
	unit:DecaM3 (http://qudt.org/vocab/unit/DecaM3)
	unit:TON_SHIPPING_US (http://qudt.org/vocab/unit/TON_SHIPPING_US)
	unit:GI_UK (http://qudt.org/vocab/unit/GI_UK)
	unit:GI_US (http://qudt.org/vocab/unit/GI_US)
	C (http://qudt.org/vocab/unit/CORD)
```

### List applicable Units for a QuantityKind:
Which units are applicable for PressureRatio?
```
	for (String unitIri : Qudt.QuantityKinds.PressureRatio.getApplicableUnits()) {
		Unit unit = Qudt.unit(unitIri);
		System.out.println("  " + unit + " (" + unit.getIri() + ")");
	}
	-->
	unit:MegaPA-PER-BAR (http://qudt.org/vocab/unit/MegaPA-PER-BAR)
	unit:HectoPA-PER-BAR (http://qudt.org/vocab/unit/HectoPA-PER-BAR)
	unit:PA-PER-BAR (http://qudt.org/vocab/unit/PA-PER-BAR)
	unit:KiloPA-PER-BAR (http://qudt.org/vocab/unit/KiloPA-PER-BAR)
	unit:MilliBAR-PER-BAR (http://qudt.org/vocab/unit/MilliBAR-PER-BAR)
	unit:PSI-PER-PSI (http://qudt.org/vocab/unit/PSI-PER-PSI)
	unit:BAR-PER-BAR (http://qudt.org/vocab/unit/BAR-PER-BAR)
```
### Instantiate unit by its label
Instantiating unit by label 'Pint (UK)':
```
	Qudt.unitFromLabel("Pint (UK)"));
	-->
	unit:PINT_UK
```

## Acknowledgments

This project has been developed at the [Research Studio Smart Application Technologies](https://sat.researchstudio.at) in the project ‘BIM-Interoperables Merkmalservice’, funded by the
Austrian Research Promotion Agency and Österreichische Bautechnik Veranstaltungs GmbH.
