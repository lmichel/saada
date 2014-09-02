package saadadb.dataloader.mapping;

import java.text.ParseException;

import saadadb.command.ArgsParser;
import saadadb.enums.MappingMode;
import saadadb.exceptions.FatalException;
import saadadb.util.DateUtils;
import saadadb.util.Messenger;

public class TimeMapping extends AxisMapping {

	TimeMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"t_min", "t_max", "t_exptime"}, entryMode);
		String s;
		if( (s = ap.getTmin(entryMode)) != null  ){
			try {
				if((s.startsWith("'") && s.endsWith("'")))
				{
					//s=DateUtils.getMJD(s);
					this.columnMapping.put("t_min", new ColumnMapping(null,s, "t_min"));

				}
				this.columnMapping.put("t_min", new ColumnMapping(null, s, "t_min"));

				//String v = DateUtils.getMJD(s);
				//this.columnMapping.put("t_min", new ColumnMapping(null, "'" + v + "'", "t_min"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_min: Cannot parse the date " + s  + ": ignored");
			}
		}
		if( (s = ap.getTmax(entryMode)) != null  ){
			ColumnMapping cm = null;
			try {
				cm = new ColumnMapping(null, s, "t_max");
				if( cm.byValue()) {
					cm.setExpression(DateUtils.getMJD(cm.getExpression()));
				}
				this.columnMapping.put("t_max",cm);
				System.out.println(cm);
//				if( )
//				
//				if(s.contains("'"))
//				{
//					s=DateUtils.getMJD(s);
//				}
//				this.columnMapping.put("t_max", new ColumnMapping(null, s, "t_max"));
//				String v = DateUtils.getMJD(s);
//				this.columnMapping.put("t_max", new ColumnMapping(null, "'" +v + "'", "t_max"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_max: Cannot parse the date <" + s  + ">: ignored");
				cm = new ColumnMapping(MappingMode.NOMAPPING, null, null, null);
			}
		}
		if( (s = ap.getExpTime(entryMode)) != null  ){
			this.columnMapping.put("t_exptime", new ColumnMapping(null, s, "t_exptime"));
		} 
		this.priority = ap.getTimeMappingPriority();
		this.completeColumns();
	}

	public static void main(String[] args) throws ParseException, FatalException{
		TimeMapping tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=\'12/12/2012\'", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=\'56273.0\'", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin='10'+emax", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=t_max-t_exptime", "eeeee"}), false);
		System.out.println(tm);
	}
}
