package saadadb.dataloader.mapping;

import java.text.ParseException;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.Messenger;

/**
 * @author michel
 * @version $Id$
 */
public class TimeMapping extends AxisMapping {

	/**
	 * @param ap
	 * @param entryMode
	 * @throws FatalException
	 */
	TimeMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"t_min", "t_max", "t_exptime"}, entryMode);
		String s;
		if( (s = ap.getTmin(entryMode)) != null  ){
			try {
				if((s.startsWith("'") && s.endsWith("'"))) {
					this.columnMapping.put("t_min", new ColumnMapping(null,s, "t_min"));
				}
				this.columnMapping.put("t_min", new ColumnMapping(null, s, "t_min"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_min: Cannot parse the date " + s  + ": ignored");
			}
		}
		if( (s = ap.getTmax(entryMode)) != null  ){
			try {
				if((s.startsWith("'") && s.endsWith("'"))) {
					this.columnMapping.put("t_max", new ColumnMapping(null,s, "t_max"));
				}
				this.columnMapping.put("t_max", new ColumnMapping(null, s, "t_max"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_max: Cannot parse the date " + s  + ": ignored");
			}
		}
		if( (s = ap.getExpTime(entryMode)) != null  ){
			this.columnMapping.put("t_exptime", new ColumnMapping(null, s, "t_exptime"));
		} 
		this.priority = ap.getTimeMappingPriority();
		this.completeColumns();
	}

	/**
	 * @param args
	 * @throws ParseException
	 * @throws FatalException
	 */
	public static void main(String[] args) throws ParseException, FatalException{
		TimeMapping tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=\'12/12/2012\'", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=\'56273.0\'", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin='10'+emax", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=t_max-t_exptime", "-exptime=1000"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=WCS.getMin(1)", "-tmax=Column.getMin(RA)"}), false);
		System.out.println(tm);
		
		StringBuffer message = new StringBuffer("eaze zaezae Converte dsqdsqdsqdsq qdqds");
		int pos = message.indexOf("Conv");
		if( pos == -1){
			message.append("Conv2");
		} else {
			message.replace(pos, message.length(), "Conv2");
		}

		System.out.println(message);
	}
}
