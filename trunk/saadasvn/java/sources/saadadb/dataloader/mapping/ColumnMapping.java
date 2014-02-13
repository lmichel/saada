package saadadb.dataloader.mapping;

public class ColumnMapping {

	private MappingMode mappingMode = MappingMode.NOMAPPING;
	private String unit;
	private String[] valueRange;
	
	
	public boolean byValue() {
		return(mappingMode == MappingMode.VALUE );
	}
	public boolean byAttribute() {
		return(mappingMode == MappingMode.ATTRIBUTE );
	}
	public boolean bySql() {
		return(mappingMode == MappingMode.SQL);
	}
	public boolean notMapped() {
		return(mappingMode == MappingMode.NOMAPPING);
	}

	/**
	 * @return
	 */
	public String[] getValueRange() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return new String[]{valueRange[0], valueRange[valueRange.length - 1]};
		}
	}

	/**
	 * @return
	 */
	public String getValue() {
		if (mappingMode == MappingMode.NOMAPPING) {
			return null;
		} else {
			return valueRange[0];
		}
	}
}
