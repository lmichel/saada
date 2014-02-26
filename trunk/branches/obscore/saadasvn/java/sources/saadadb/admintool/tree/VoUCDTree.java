package saadadb.admintool.tree;

import java.awt.Frame;


public class VoUCDTree extends VoTree {
	/**
	 * * @version $Id: VoUCDTree.java 118 2012-01-06 14:33:51Z laurent.mistahl $
 
	 */
	private static final long serialVersionUID = 1L;
	String[] flat_ucd = {
			"arith.diff (Difference between two quantities described by the same UCD)",
			"arith.factor (Numerical factor)",
			"arith.grad (Gradient)",
			"arith.rate (Rate (per time unit))",
			"arith.ratio (Ratio between two quantities described by the same UCD)",
			"arith.zp (Zero point)",
			"em.IR (Infrared part of the spectrum)",
			"em.IR.15-30um (Infrared between 15 and 30 micron)",
			"em.IR.3-4um (Infrared between 3 and 4 micron)",
			"em.IR.30-60um (Infrared between 30 and 60 micron)",
			"em.IR.4-8um (Infrared between 4 and 8 micron)",
			"em.IR.60-100um (Infrared between 60 and 100 micron)",
			"em.IR.8-15um (Infrared between 8 and 15 micron)",
			"em.IR.FIR (Far-Infrared, 30-100 microns)",
			"em.IR.H (Infrared between 1.5 and 2 micron)",
			"em.IR.J (Infrared between 1.0 and 1.5 micron)",
			"em.IR.K (Infrared between 2 and 3 micron)",
			"em.IR.MIR (Medium-Infrared, 5-30 microns)",
			"em.IR.NIR (Near-Infrared, 1-5 microns)",
			"em.UV (Ultraviolet part of the spectrum)",
			"em.UV.10-50nm (Ultraviolet between 10 and 50 nm)",
			"em.UV.100-200nm (Ultraviolet between 100 and 200 nm)",
			"em.UV.200-300nm (Ultraviolet between 200 and 300 nm)",
			"em.UV.50-100nm (Ultraviolet between 50 and 100 nm)",
			"em.UV.FUV (Far-Ultraviolet)",
			"em.X-ray (X-ray part of the spectrum)",
			"em.X-ray.hard (Hard X-ray (12 - 120 keV))",
			"em.X-ray.medium (Medium X-ray (2 - 12 keV))",
			"em.X-ray.soft (Soft X-ray (0.12 - 2 keV))",
			"em.bin (Channel / instrumental spectral bin coordinate (bin number))",
			"em.energy (Energy value in the em frame)",
			"em.freq (Frequency value in the em frame)",
			"em.gamma (Gamma rays part of the spectrum)",
			"em.gamma.hard (Hard gamma ray (>500 keV))",
			"em.gamma.soft (Soft gamma ray (120 - 500 keV))",
			"em.line (Designation of major atomic lines)",
			"em.line.HI (21cm hydrogen line)",
			"em.line.Brgamma (Bracket gamma line)",
			"em.line.Halpha (H-alpha line)",
			"em.line.Hbeta (H-beta line)",
			"em.line.Hgamma (H-gamma line)",
			"em.line.Hdelta (H-delta line)",
			"em.line.Lyalpha (H-Lyalpha line)",
			"em.line.OIII ([OIII] line whose rest wl is 500.7 nm)",
			"em.line.CO (CO radio line, e.g. 12CO(1-0) rest wl 115GHz)",
			"em.mm (Millimetric part of the spectrum)",
			"em.mm.100-200GHz (Millimetric between 100 and 200 GHz)",
			"em.mm.1500-3000GHz (Millimetric between 1500 and 3000 GHz)",
			"em.mm.200-400GHz (Millimetric between 200 and 400 GHz)",
			"em.mm.30-50GHz (Millimetric between 30 and 50 GHz)",
			"em.mm.400-750GHz (Millimetric between 400 and 750 GHz)",
			"em.mm.50-100GHz (Millimetric between 50 and 100 GHz)",
			"em.mm.750-1500GHz (Millimetric between 750 and 1500 GHz)",
			"em.opt (Optical part of the spectrum)",
			"em.opt.B (Optical band between 400 and 500 nm)",
			"em.opt.I (Optical band between 750 and 1000 nm)",
			"em.opt.R (Optical band between 600 and 750 nm)",
			"em.opt.U (Optical band between 300 and 400 nm)",
			"em.opt.V (Optical band between 500 and 600 nm)",
			"em.radio (Radio part of the spectrum)",
			"em.radio.100-200MHz (Radio between 100 and 200 MHz)",
			"em.radio.12-30GHz (Radio between 12 and 30 GHz)",
			"em.radio.1500-3000MHz (Radio between 1500 and 3000 MHz)",
			"em.radio.20-100MHz (Radio between 20 and 100 MHz)",
			"em.radio.200-400MHz (Radio between 200 and 400 MHz)",
			"em.radio.3-6GHz (Radio between 3 and 6 GHz)",
			"em.radio.400-750MHz (Radio between 400 and 750 MHz)",
			"em.radio.6-12GHz (Radio between 6 and 12 GHz)",
			"em.radio.750-1500MHz (Radio between 750 and 1500 MHz)",
			"em.wavenumber (Wavenumber value in the em frame)",
			"em.wl (Wavelength value in the em frame)",
			"em.wl.central (Central wavelength)",
			"em.wl.effective (Effective wavelength)",
			"instr.background (Instrumental background)",
			"instr.bandpass (Bandpass (eg: band name) of instrument)",
			"instr.bandwidth (Bandwidth of the instrument)",
			"instr.baseline (Baseline for interferometry)",
			"instr.beam (Beam)",
			"instr.calib (Calibration parameter)",
			"instr.det (Detector)",
			"instr.det.noise (Instrument noise)",
			"instr.det.psf (Point Spread Function)",
			"instr.det.qe (Quantum efficiency)",
			"instr.dispersion (Dispersion of a spectrograph)",
			"instr.filter (Filter)",
			"instr.fov (Field of view)",
			"instr.obsty (Observatory, satellite, mission)",
			"instr.obsty.seeing (Seeing)",
			"instr.offset (Offset angle respect to main direction of observation)",
			"instr.order (Spectral order in a spectrograph)",
			"instr.param (Various instrumental parameters)",
			"instr.pixel (Pixel (default size: angular))",
			"instr.plate (Photographic plate)",
			"instr.plate.emulsion (Plate emulsion)",
			"instr.precision (Instrument precision)",
			"instr.saturation (Instrument saturation threshold)",
			"instr.scale (Instrument scale (for CCD, plate, image))",
			"instr.sensitivity (Instrument sensitivity, detection threshold)",
			"instr.setup (Instrument configuration or setup)",
			"instr.skyLevel (Sky level)",
			"instr.skyTemp (Sky temperature)",
			"instr.tel (Telescope)",
			"instr.tel.focalLength (Telescope focal length)",
			"meta.abstract (Abstract (of paper, proposal,etc))",
			"meta.bib (Bibliographic reference)",
			"meta.bib.author (Author name)",
			"meta.bib.bibcode (Bibcode)",
			"meta.bib.fig (Figure in a paper)",
			"meta.bib.journal (Journal name)",
			"meta.bib.page (Page number)",
			"meta.bib.volume (Volume number)",
			"meta.code (Code or flag)",
			"meta.code.class (Classification code)",
			"meta.code.error (limit uncertainty error flag)",
			"meta.code.member (Membership code)",
			"meta.code.mime (MIME type)",
			"meta.code.multip (Multiplicity or binarity flag)",
			"meta.code.qual (Quality, precision, reliability flag or code)",
			"meta.code.status (Status code (e.g.: status of a proposal/observation))",
			"meta.cryptic (Unknown or impossible to understand quantity)",
			"meta.curation (Identity of man/organization responsible for the data)",
			"meta.dataset (Dataset)",
			"meta.email (Curation/contact e-mail)",
			"meta.file (File)",
			"meta.fits (FITS standard)",
			"meta.id (Identifier, name or designation)",
			"meta.id.assoc (Identifier of associated counterpart)",
			"meta.id.CoI (Name of Co-Investigator)",
			"meta.id.cross (Cross identification)",
			"meta.id.parent (Identification of parent source)",
			"meta.id.part (Part of identifier, suffix or sub-component)",
			"meta.id.PI (Name of Principal Investigator)",
			"meta.main (Main value of something)",
			"meta.modelled (Quantity was produced by a model)",
			"meta.note (Note or remark (longer than a code or flag))",
			"meta.number (Number (of things; eg nb of object in an image))",
			"meta.record (Record number)",
			"meta.ref (Reference, or origin)",
			"meta.ref.ivorn (IVORN, Int. VO Resource Name (ivo://))",
			"meta.ref.uri (URI, universal resource identifier)",
			"meta.ref.url (URL, web address)",
			"meta.software (Software used in generating data)",
			"meta.table (Table or catalogue)",
			"meta.title (Title or explanation)",
			"meta.ucd (UCD)",
			"meta.unit (Unit)",
			"meta.version (Version)",
			"obs.airMass (Airmass)",
			"obs.atmos (Atmosphere, atmospheric phenomena affecting an observation)",
			"obs.atmos.extinction (Atmospheric extinction)",
			"obs.atmos.refractAngle (Atmospheric refraction angle)",
			"obs.calib (Calibration observation)",
			"obs.calib.flat (Related to flat-field calibration observation (dome, sky etc))",
			"obs.exposure (Exposure)",
			"obs.field (Region covered by the observation)",
			"obs.image (Image)",
			"obs.observer (Observer, discoverer)",
			"obs.param (Various observation or reduction parameter)",
			"obs.proposal (Observation proposal)",
			"obs.proposal.cycle (Proposal cycle)",
			"obs.sequence (Sequence of observations, exposures or events)",
			"phot.antennaTemp (Antenna temperature)",
			"phot.calib (Photometric calibration)",
			"phot.color (Color index or magnitude difference)",
			"phot.color.excess (color excess)",
			"phot.color.reddFree (Dereddened color)",
			"phot.count (Flux expressed in counts)",
			"phot.fluence (fluence)",
			"phot.flux (Photon flux)",
			"phot.flux.bol (Bolometric flux)",
			"phot.flux.density (Flux density (per wl/freq/energy interval))",
			"phot.flux.density.sb (Flux density surface brightness)",
			"phot.flux.sb (Flux surface brightness)",
			"phot.limbDark (Limb-darkening coefficients)",
			"phot.mag (Photometric magnitude)",
			"phot.mag.bc (Bolometric correction)",
			"phot.mag.bol (Bolometric magnitude)",
			"phot.mag.distMod (Distance modulus)",
			"phot.mag.reddFree (Dereddened magnitude)",
			"phot.mag.sb (Surface brightness in magnitude units)",
			"phys.SFR (Star formation rate)",
			"phys.absorption (Extinction or absorption along the line of sight)",
			"phys.absorption.coeff (Absorption coefficient (eg in a spectral line))",
			"phys.absorption.gal (Galactic extinction)",
			"phys.absorption.opticalDepth (Optical depth)",
			"phys.abund (Abundance)",
			"phys.abund.Fe (Fe/H abundance)",
			"phys.abund.X (Hydrogen abundance)",
			"phys.abund.Y (Helium abundance)",
			"phys.abund.Z (Metallicity abundance)",
			"phys.acceleration (Acceleration)",
			"phys.albedo (Albedo or reflectance)",
			"phys.angArea (Angular area)",
			"phys.angMomentum (Angular momentum)",
			"phys.angSize (Angular size width diameter dimension extension major minor axis extraction radius)",
			"phys.angSize.smajAxis (angular size extent or extension of semi-major axis)",
			"phys.angSize.sminAxis (angular size extent or extension of semi-minor axis)",
			"phys.area (Area (in linear units))",
			"phys.atmol (Atomic and molecular physics (shared properties))",
			"phys.atmol.branchingRatio (Branching ratio)",
			"phys.atmol.collisional (Related to collisions)",
			"phys.atmol.collStrength (Collisional strength)",
			"phys.atmol.configuration (Configuration)",
			"phys.atmol.crossSection (Atomic / molecular cross-section)",
			"phys.atmol.element (Element)",
			"phys.atmol.excitation (Atomic molecular excitation parameter)",
			"phys.atmol.final (Quantity refers to atomic/molecular final/ground state, level, ecc.)",
			"phys.atmol.initial (Quantity refers to atomic/molecular initial state, level, ecc)",
			"phys.atmol.ionStage (Ion, ionization stage)",
			"phys.atmol.ionization (Related to ionization)",
			"phys.atmol.lande (Lande factor)",
			"phys.atmol.level (Atomic level)",
			"phys.atmol.lifetime (Lifetime of a level)",
			"phys.atmol.lineShift (Line shifting coefficient)",
			"phys.atmol.number (Atomic number Z)",
			"phys.atmol.oscStrength (Oscillator strength)",
			"phys.atmol.parity (Parity)",
			"phys.atmol.qn (Quantum number)",
			"phys.atmol.radiationType (Type of radiation characterizing atomic lines (electric dipole/quadrupole, magnetic dipole))",
			"phys.atmol.symmetry (Type of nuclear spin symmetry)",
			"phys.atmol.sWeight (Statistical weight)",
			"phys.atmol.sWeight.nuclear (Statistical weight for nuclear spin states)",
			"phys.atmol.term (Atomic term)",
			"phys.atmol.transition (Transition between states)",
			"phys.atmol.transProb (Transition probability, Einstein A coefficient)",
			"phys.atmol.wOscStrength (Weighted oscillator strength)",
			"phys.atmol.weight (Atomic weight)",
			"phys.columnDensity (Column density)",
			"phys.composition (Quantities related to composition of objects)",
			"phys.composition.massLightRatio (Mass to light ratio)",
			"phys.composition.yield (Mass yield)",
			"phys.cosmology (Related to cosmology)",
			"phys.damping (Generic damping quantities)",
			"phys.density (Density (of mass, electron etc))",
			"phys.dielectric (Complex dielectric function)",
			"phys.dispMeasure (Dispersion measure)",
			"phys.electField (Electric field)",
			"phys.electron (Electron)",
			"phys.electron.degen (Electron degeneracy parameter)",
			"phys.emissMeasure (Emission measure)",
			"phys.emissivity (Emissivity)",
			"phys.energy (Energy)",
			"phys.energy.density (Energy-density)",
			"phys.entropy (Entropy)",
			"phys.eos (Equation of state)",
			"phys.excitParam (Excitation parameter U)",
			"phys.gauntFactor (Gaunt factor/correction)",
			"phys.gravity (Gravity)",
			"phys.ionizParam (Ionization parameter)",
			"phys.ionizParam.coll (Collisional ionization)",
			"phys.ionizParam.rad (Radiative ionization)",
			"phys.luminosity (Luminosity)",
			"phys.luminosity.fun (Luminosity function)",
			"phys.magAbs (Absolute magnitude)",
			"phys.magAbs.bol (Bolometric absolute magnitude)",
			"phys.magField (Magnetic field)",
			"phys.mass (Mass)",
			"phys.mass.loss (Mass loss)",
			"phys.mol (Molecular data)",
			"phys.mol.dipole (Molecular dipole)",
			"phys.mol.dipole.electric (Molecular electric dipole moment)",
			"phys.mol.dipole.magnetic (Molecular magnetic dipole moment)",
			"phys.mol.dissociation (Molecular dissociation)",
			"phys.mol.formationHeat (Formation heat for molecules)",
			"phys.mol.quadrupole (Molecular quadrupole)",
			"phys.mol.quadrupole.electric (Molecular electric quadrupole moment)",
			"phys.mol.rotation (Molecular rotation)",
			"phys.mol.vibration (Molecular vibration)",
			"phys.particle.neutrino (Related to neutrino)",
			"phys.polarization (Polarization degree (or percentage))",
			"phys.polarization.circular (Circular polarization)",
			"phys.polarization.linear (Linear polarization)",
			"phys.polarization.rotMeasure (Rotation measure polarization)",
			"phys.polarization.stokes (Stokes polarization)",
			"phys.pressure (Pressure)",
			"phys.recombination.coeff (Recombination coefficient)",
			"phys.refractIndex (Refraction index)",
			"phys.size (Linear size, length (not angular))",
			"phys.size.axisRatio (Axis ratio (a/b) or (b/a))",
			"phys.size.diameter (Diameter)",
			"phys.size.radius (Radius)",
			"phys.size.smajAxis (Linear semi major axis)",
			"phys.size.sminAxis (Linear semi minor axis)",
			"phys.temperature (Temperature)",
			"phys.temperature.effective (Effective temperature)",
			"phys.temperature.electron (Electron temperature)",
			"phys.transmission (Transmission (of filter, instrument etc))",
			"phys.veloc (Space velocity)",
			"phys.veloc.ang (Angular velocity)",
			"phys.veloc.dispersion (Velocity dispersion)",
			"phys.veloc.escape (Escape velocity)",
			"phys.veloc.expansion (Expansion velocity)",
			"phys.veloc.microTurb (Microturbulence velocity)",
			"phys.veloc.orbital (Orbital velocity)",
			"phys.veloc.pulsat (Pulsational velocity)",
			"phys.veloc.rotat (Rotational velocity)",
			"phys.veloc.transverse (Transverse / tangential velocity)",
			"phys.virial (Related to virial quantities (mass, radius etc))",
			"pos.angDistance (Angular distance, elongation)",
			"pos.angResolution (Angular resolution)",
			"pos.az (Position in alt-azimutal frame)",
			"pos.az.alt (Alt-azimutal altitude)",
			"pos.az.azi (Alt-azimutal azimut)",
			"pos.az.zd (Alt-azimutal zenith distance)",
			"pos.barycenter (Barycenter)",
			"pos.bodyrc (Body related coordinates)",
			"pos.bodyrc.alt (Body related coordinate (altitude on the body))",
			"pos.bodyrc.lat (Body related coordinate (latitude on the body))",
			"pos.bodyrc.long (Body related coordinate (longitude on the body))",
			"pos.cartesian (Cartesian (rectangular) coordinates)",
			"pos.cartesian.x (Cartesian coordinate along the x-axis)",
			"pos.cartesian.y (Cartesian coordinate along the y-axis)",
			"pos.cartesian.z (Cartesian coordinate along the z-axis)",
			"pos.cmb (Cosmic Microwave Background reference frame)",
			"pos.dirCos (Direction cosine)",
			"pos.distance (Linear distance)",
			"pos.earth (Coordinates related to Earth)",
			"pos.earth.altitude (Altitude, height on Earth above sea level)",
			"pos.earth.lat (Latitude on Earth)",
			"pos.earth.lon (Longitude on Earth)",
			"pos.ecliptic (Ecliptic coordinates)",
			"pos.ecliptic.lat (Ecliptic latitude)",
			"pos.ecliptic.lon (Ecliptic longitude)",
			"pos.eop (Earth orientation parameters)",
			"pos.eop.nutation (Earth nutation)",
			"pos.ephem (Ephemeris)",
			"pos.eq (Equatorial coordinates)",
			"pos.eq.dec (Declination in equatorial coordinates)",
			"pos.eq.ha (Hour-angle)",
			"pos.eq.ra (Right ascension in equatorial coordinates)",
			"pos.eq.spd (South polar distance in equatorial coordinates)",
			"pos.errorEllipse (Positional error ellipse)",
			"pos.frame (Reference frame used for positions)",
			"pos.galactic (Galactic coordinates)",
			"pos.galactic.lat (Latitude in galactic coordinates)",
			"pos.galactic.lon (Longitude in galactic coordinates)",
			"pos.galactocentric (Galactocentric coordinate system)",
			"pos.geocentric (Geocentric coordinate system)",
			"pos.healpix (Hierarchical Equal Area IsoLatitude Pixelization)",
			"pos.heliocentric (Heliocentric position coordinate (solar system bodies))",
			"pos.HTM (Hierarchical Triangular Mesh)",
			"pos.lambert (Lambert projection)",
			"pos.lg (Local Group reference frame)",
			"pos.lsr (Local Standard of Rest reference frame)",
			"pos.lunar (Lunar coordinates)",
			"pos.lunar.occult (Occultation by lunar limb)",
			"pos.parallax (Parallax)",
			"pos.parallax.dyn (Dynamical parallax)",
			"pos.parallax.phot (Photometric parallaxes)",
			"pos.parallax.spect (Spectroscopic parallax)",
			"pos.parallax.trig (Trigonometric parallax)",
			"pos.phaseAng (Phase angle, e-g- elongation of earth from sun as seen from a third cel- object)",
			"pos.pm (Proper motion)",
			"pos.posAng (Position angle of a given vector)",
			"pos.precess (Precession (in equatorial coordinates))",
			"pos.supergalactic (Supergalactic coordinates)",
			"pos.supergalactic.lat (Latitude in supergalactic coordinates)",
			"pos.supergalactic.lon (Longitude in supergalactic coordinates)",
			"pos.wcs (WCS keywords)",
			"pos.wcs.cdmatrix (WCS CDMATRIX)",
			"pos.wcs.crpix (WCS CRPIX)",
			"pos.wcs.crval (WCS CRVAL)",
			"pos.wcs.ctype (WCS CTYPE)",
			"pos.wcs.naxes (WCS NAXES)",
			"pos.wcs.naxis (WCS NAXIS)",
			"pos.wcs.scale (WCS scale or scale of an image)",
			"spect.binSize (Spectral bin size)",
			"spect.continuum (Continuum spectrum)",
			"spect.dopplerParam (Doppler parameter b)",
			"spect.dopplerVeloc (Radial velocity, derived from the shift of some spectral feature)",
			"spect.dopplerVeloc.opt (Radial velocity derived from a wavelength shift using the optical convention)",
			"spect.dopplerVeloc.radio (Radial velocity derived from a frequency shift using the radio convention)",
			"spect.index (Spectral index)",
			"spect.line (Spectral line)",
			"spect.line.asymmetry (Line asymmetry)",
			"spect.line.broad (Spectral line broadening)",
			"spect.line.broad.Stark (Stark line broadening coefficient)",
			"spect.line.broad.Zeeman (Zeeman broadening)",
			"spect.line.eqWidth (Line equivalent width)",
			"spect.line.intensity (Line intensity)",
			"spect.line.profile (Line profile)",
			"spect.line.strength (Spectral line strength S)",
			"spect.line.width (Spectral line fwhm)",
			"spect.resolution (Spectral (or velocity) resolution)",
			"src.calib (Calibration source)",
			"src.calib.guideStar (Guide star)",
			"src.class (Source classification (star, galaxy, cluster etc))",
			"src.class.color (Color classification)",
			"src.class.distance (Distance class e.g. Abell)",
			"src.class.luminosity (Luminosity class)",
			"src.class.richness (Richness class e.g. Abell)",
			"src.class.starGalaxy (Star/galaxy discriminator, stellarity index)",
			"src.class.struct (Structure classification e.g. Bautz-Morgan)",
			"src.density (Density of sources)",
			"src.ellipticity (Source ellipticity)",
			"src.impactParam (Impact parameter)",
			"src.morph (Morphology structure)",
			"src.morph.param (Morphological parameter)",
			"src.morph.scLength (Scale length for a galactic component (disc or bulge))",
			"src.morph.type (Hubble morphological type (galaxies))",
			"src.net (Qualifier indicating that a quantity (e.g. flux) is background subtracted rather than total)",
			"src.orbital (Orbital parameters)",
			"src.orbital.eccentricity (Orbit eccentricity)",
			"src.orbital.inclination (Orbit inclination)",
			"src.orbital.meanAnomaly (Orbit mean anomaly)",
			"src.orbital.meanMotion (Mean motion)",
			"src.orbital.node (Ascending node)",
			"src.orbital.periastron (Periastron)",
			"src.redshift (Redshift)",
			"src.redshift.phot (Photometric redshift)",
			"src.sample (Sample)",
			"src.spType (Spectral type MK)",
			"src.var (Variability of source)",
			"src.var.amplitude (Amplitude of variation)",
			"src.var.index (Variability index)",
			"src.var.pulse (Pulse)",
			"stat.Fourier (Fourier coefficient)",
			"stat.Fourier.amplitude (Amplitude Fourier coefficient)",
			"stat.correlation (Correlation between two parameters)",
			"stat.covariance (Covariance between two parameters)",
			"stat.error (Statistical error)",
			"stat.error.sys (Systematic error)",
			"stat.filling (Filling factor (volume, time, etc))",
			"stat.fit (Fit)",
			"stat.fit.chi2 (Chi2)",
			"stat.fit.dof (Degrees of freedom)",
			"stat.fit.goodness (Goodness or significance of fit)",
			"stat.fit.omc (Observed minus computed)",
			"stat.fit.param (Parameter of fit)",
			"stat.fit.residual (Residual fit)",
			"stat.likelihood (Likelihood)",
			"stat.max (Maximum or upper limit)",
			"stat.mean (Mean, average value)",
			"stat.median (Median value)",
			"stat.min (Minimum or lowest limit)",
			"stat.param (Parameter)",
			"stat.probability (Probability)",
			"stat.snr (Signal to noise ratio)",
			"stat.stdev (Standard deviation)",
			"stat.uncalib (Qualifier of a generic incalibrated quantity)",
			"stat.value (Miscellaneous value)",
			"stat.variance (Variance)",
			"stat.weight (Statistical weight)",
			"time.age (Age)",
			"time.creation (Creation time/date (of dataset, file, catalogue, etc))",
			"time.crossing (Crossing time)",
			"time.duration (Interval of time describing the duration of a generic event or phenomenon)",
			"time.end (End time/date of a generic event)",
			"time.epoch (Instant of time related to a generic event (epoch, date, Julian date, time stamp/tag, etc))",
			"time.equinox (Equinox)",
			"time.interval (Time interval, time-bin, time elapsed between two events, not the duration of an event)",
			"time.lifetime (Lifetime)",
			"time.period (Period, interval of time between the recurrence of phases in a periodic phenomenon)",
			"time.phase (Phase, position within a period)",
			"time.processing (A time/date associated with the processing of data)",
			"time.publiYear (Publication year)",
			"time.relax (Relaxation time)",
			"time.release (The time/date data is available to the public)",
			"time.resolution (Time resolution)",
			"time.scale (Timescale)",
			"time.start (Start time/date of generic event)"};

	public VoUCDTree(Frame frame) {
		super(frame, "UCDs (drag & drop to the meta-data panel)");
		flat_types = flat_ucd;
	}

	/* (non-Javadoc)
	 * @see gui.VOTree#getPathComponents(java.lang.String)
	 */
	protected String[] getPathComponents(String string) {
		String[] utype_tokens = string.split("\\.");
		utype_tokens[utype_tokens.length - 1] = string;
		return utype_tokens;
	}
	
	protected void setDragFeatures() {
		this.tree.setDragEnabled(true);
	}


}
