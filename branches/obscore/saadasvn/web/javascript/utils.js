
/*
 * Unit array used to setup UCD based queries
 */
var unitMap = new Array();
unitMap['Energy']    = ['erg', 'eV', 'keV', 'MeV', 'GeV', 'TeV', 'J', 'ryd'];
unitMap['Frequency'] = ['Hz', 'KHz', 'MHz', 'GHz', 'THz'];
unitMap['Time']      = ['y', 'd', 'h', 'mn', 'sec', 'msec', 'nsec'];
unitMap['Length']    = ['kpc', 'pc', 'AU', 'km', 'm', 'cm', 'mm', 'um', 'nm', 'Angstroem'];
unitMap['Velocity']  = ['m/s', 'km/s', 'km/h', 'mas/yr'];
unitMap['Angle']     = ['deg', 'arcmin', 'arcsec'];
unitMap['Flux']      = ['erg/s/cm2', 'Jy', 'mJy', 'count/s'];
unitMap['Power']     = ['erg/s', 'W'];

var units =  [
              {id: 'none', text: "none"}

              , {id: 'Power_erg/s', text: "erg/s"}
              , {id: 'Power_W', text: "W"}

              , {id: 'Flux_erg/s/cm2', text: "erg/s/cm2"}
              , {id: 'Flux_Jy', text: "Jy"}
              , {id: 'Flux_mJy', text: "mJy"}
              , {id: 'Flux_mJy', text: "mJy"}

              , {id: 'Angle_deg', text: "deg"}
              , {id: 'Angle_arcmin', text: "arcmin"}
              , {id: 'Angle_arcsec', text: "arcsec"}
              , {id: 'Angle_h:m:s', text: "h:m:s"}

              , {id: 'Velocity_m/s', text: "m/s"}
              , {id: 'Velocity_km/s', text: "km/s"}
              , {id: 'Velocity_km/h', text: "km/h"}
              , {id: 'Velocity_mas/yr', text: "mas/yr"}

              , {id: 'Length_kpc', text: "kpc"}
              , {id: 'Length_pc', text: "pc"}
              , {id: 'Length_AU', text: "AU"}
              , {id: 'Length_km', text: "km"}
              , {id: 'Length_m', text: "m"}
              , {id: 'Length_cm', text: "cm"}
              , {id: 'Length_mm', text: "mm"}
              , {id: 'Length_um', text: "um"}
              , {id: 'Length_nm', text: "nm"}
              , {id: 'Length_Angstroem', text: "Angstroem"}

              , {id: 'Energy_erg', text: "erg"}
              , {id: 'Energy_eV', text: "eV"}
              , {id: 'Energy_keV', text: "keV"}
              , {id: 'Energy_MeV', text: "MeV"}
              , {id: 'Energy_GeV', text: "GeV"}
              , {id: 'Energy_TeV', text: "TeV"}
              , {id: 'Energy_J', text: "J"}
              , {id: 'Energy_ryd', text: "ryd"}

              , {id: 'Frequency_Hz', text: "Hz"}
              , {id: 'Frequency_KHz', text: "KHz"}
              , {id: 'Frequency_MHz', text: "MHz"}
              , {id: 'Frequency_GHz', text: "GHz"}
              , {id: 'Frequency_THz', text: "THz"}

              , {id: 'Time_y', text: "y"}
              , {id: 'Time_d', text: "d"}
              , {id: 'Time_h', text: "h"}
              , {id: 'Time_mn', text: "mn"}
              , {id: 'Time_sec', text: "sec"}
              , {id: 'Time_msec', text: "msec"}
              , {id: 'Time_nsec', text: "nsec"}
              ];
function setTitlePath(treepath) {
	globalTreePath = treepath;

	$('#titlepath').html('<i>');
	for( var i=0 ; i<treepath.length  ; i++ ) {
		if( i > 0 ) $('#titlepath').append('&gt;');
		$('#titlepath').append(treepath[i]);
	}
}

var globalTreePath = new Array();
function setTitlePath(treepath) {
	globalTreePath = treepath;

	$('#titlepath').html('<i>');
	for( var i=0 ; i<treepath.length  ; i++ ) {
		if( i > 0 ) $('#titlepath').append('&gt;');
		$('#titlepath').append(treepath[i]);
	}
}

function getTreePathAsKey() {
	var retour = '';
	for( var i=0 ; i<globalTreePath.length  ; i++ ) {
		if( i > 0 )retour += '_';
		retour += globalTreePath[i];
	}
	return retour;
}

function switchArrow(id) {
	var image = $('#'+id+'').find('img').attr('src');
	if (image == 'images/tdown.png') {
		$('#'+id+'').find('img').attr('src', 'images/tright.png');
	} else if (image == 'images/tright.png') {
		$('#'+id+'').find('img').attr('src', 'images/tdown.png');
	}
}

