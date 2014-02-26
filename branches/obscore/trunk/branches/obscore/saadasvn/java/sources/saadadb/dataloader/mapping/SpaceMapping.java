package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;
import saadadb.exceptions.FatalException;

public class SpaceMapping extends AxeMapping {

	SpaceMapping(ArgsParser ap) throws FatalException {
		super(ap, new String[]{"s_ra", "s_dec","s_fov", "error_maj_csa", "error_maj_csa", "error_angle_csa"});
		this.completeColumns();
	}

	public static void main(String[] args) throws FatalException {
		SpaceMapping om = new SpaceMapping(new ArgsParser(new String[]{"-name=abc,eee"}));
		System.out.println(om);
	}

}
