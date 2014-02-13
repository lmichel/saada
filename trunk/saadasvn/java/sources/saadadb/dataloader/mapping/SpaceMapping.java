package saadadb.dataloader.mapping;

import saadadb.command.ArgsParser;

public class SpaceMapping extends AxeMapping {

	SpaceMapping(ArgsParser ap) {
		super(ap, new String[]{"s_ra", "s_dec","s_fov", "error_maj_csa", "error_maj_csa", "error_angle_csa"});
	}

}
