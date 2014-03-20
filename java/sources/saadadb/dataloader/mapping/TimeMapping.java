package saadadb.dataloader.mapping;

import java.text.ParseException;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;
import saadadb.util.DateUtils;
import saadadb.util.Messenger;
import saadadb.util.RegExp;
import cds.astro.Astrotime;

public class TimeMapping extends AxisMapping {

	TimeMapping(ArgsParser ap, boolean entryMode) throws FatalException {
		super(ap, new String[]{"t_min", "t_max"}, entryMode);
		String s;
		if( (s = ap.getTmin(entryMode)) != null  ){
			try {
				String v = DateUtils.getMJD(s);
				this.columnMapping.put("t_min", new ColumnMapping(null, "'" + v + "'", "t_min"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_min: Cannot parse the date " + s  + ": ignored");
			}
		}
		if( (s = ap.getTmax(entryMode)) != null  ){
			try {
				String v = DateUtils.getMJD(s);
				this.columnMapping.put("t_max", new ColumnMapping(null, "'" +v + "'", "t_max"));
			} catch (Exception e) {
				Messenger.printMsg(Messenger.WARNING, "t_max: Cannot parse the date <" + s  + ">: ignored");
			}
		}
		this.priority = ap.getTimeMappingPriority();
		this.completeColumns();
	}

	public static void main(String[] args) throws ParseException, FatalException{
		TimeMapping tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=12/12/2012", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=56273.0", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=-123000", "eeeee"}), false);
		System.out.println(tm);
		tm = new TimeMapping(new ArgsParser(new String[]{"-tmin=fsdfsfsd", "eeeee"}), false);
		System.out.println(tm);
	}
}
