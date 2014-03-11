package saadadb.admintool.tree;

import java.awt.Frame;


/**
 * @author laurentmichel
 * * @version $Id$

 */
public class VoSpectrumDMTree extends VoTree {
	public String[] spectral_utypes = {
			"SampleExtent (em.*;spect.binSize)",
			"SamplingPrecisionRefVal.FillFactor (time;stat.)",
			"Spectrum.Char.FluxAxis.Accuracy.StatError (stat.error;phot.flux.density;em.)",
			"Spectrum.Char.FluxAxis.Accuracy.SysError (stat.error.sys;phot.flux.density;em.)",
			"Spectrum.Char.SpatialAxis.Accuracy.StatError (stat.error;pos.eq)",
			"Spectrum.Char.SpatialAxis.Accuracy.SysError (stat.error.sys;pos.eq)",
			"Spectrum.Char.SpatialAxis.Calibration (meta.code.qual)",
			"Spectrum.Char.SpatialAxis.Coverage.Bounds.Extent (instr.fov)",
			"Spectrum.Char.SpatialAxis.Coverage.Location.Value (pos.eq)",
			"Spectrum.Char.SpatialAxis.Coverage.Support.Area (Aperture)",
			"Spectrum.Char.SpatialAxis.Coverage.Support.Extent (instr.fov)",
			"Spectrum.Char.SpatialAxis.Name (meta.id)",
			"Spectrum.Char.SpatialAxis.Resolution (pos.angResolution)",
			"Spectrum.Char.SpatialAxis.ucd (meta.ucd)",
			"Spectrum.Char.SpatialAxis.unit (meta.unit)",
			"Spectrum.Char.SpectralAxis.Accuracy.BinSize (em.*;spect.binSize)",
			"Spectrum.Char.SpectralAxis.Accuracy.StatError (stat.error;em.*)",
			"Spectrum.Char.SpectralAxis.Accuracy.SysError (stat.error.sys;em.*)",
			"Spectrum.Char.SpectralAxis.Calibration (meta.code.qual)",
			"Spectrum.Char.SpectralAxis.Coverage.Bounds.Extent (instr.bandwidth)",
			"Spectrum.Char.SpectralAxis.Coverage.Bounds.Start (em.*;stat.min)",
			"Spectrum.Char.SpectralAxis.Coverage.Bounds.Stop (em.*;stat.max)",
			"Spectrum.Char.SpectralAxis.Coverage.Location.Value (instr.bandpass)",
			"Spectrum.Char.SpectralAxis.Coverage.Support.Extent (instr.bandwidth)",
			"Spectrum.Char.SpectralAxis.Resolution (spect.resolution;)",
			"Spectrum.Char.SpectralAxis.ResPower (spect.resolution)",
			"Spectrum.Char.TimeAxis.Accuracy.BinSize (time.interval)",
			"Spectrum.Char.TimeAxis.Accuracy.StatError (stat.error;time)",
			"Spectrum.Char.TimeAxis.Accuracy.SysError (stat.error.sys;time)",
			"Spectrum.Char.TimeAxis.Calibration (meta.code.qual)",
			"Spectrum.Char.TimeAxis.Coverage.Bounds.Extent (time.duration)",
			"Spectrum.Char.TimeAxis.Coverage.Bounds.Start (time.start;obs.exposure)",
			"Spectrum.Char.TimeAxis.Coverage.Bounds.Stop (time.stop;obs.exposure)",
			"Spectrum.Char.TimeAxis.Coverage.Location.Value (time.epoch)",
			"Spectrum.Char.TimeAxis.Coverage.Support.Extent (time.duration;obs.exposure)",
			"Spectrum.Char.TimeAxis.Resolution (time.resolution)",
			"Spectrum.Char.TimeAxis.SamplingPrecision. (SampleExtent)",
			"Spectrum.CoordSys.SpaceFrame.Equinox (time.equinox;pos.frame)",
			"Spectrum.CoordSys.TimeFrame.Name (time.scale)",
			"Spectrum.CoordSys.TimeFrame.RefPos (time.scale)",
			"Spectrum.CoordSys.TimeFrame.Zero (time;arith.zp)",
			"Spectrum.Curation.Contact.Email (meta.ref.url;meta.email)",
			"Spectrum.Curation.Contact.Name (meta.bib.author;meta.curation)",
			"Spectrum.Curation.PublisherDID (meta.ref.url;meta.curation)",
			"Spectrum.Curation.PublisherID (meta.ref.url;meta.curation)",
			"Spectrum.Curation.Publisher (meta.curation)",
			"Spectrum.Curation.Reference (meta.bib.bibcode)",
			"Spectrum.Curation.Version (meta.version;meta.curation)",
			"Spectrum.Data.BackgroundModel.Accuracy.StatErrHigh (phot.flux.density;)",
			"Spectrum.Data.BackgroundModel.Accuracy.StatErrLow (phot.flux.density;)",
			"Spectrum.Data.BackgroundModel.Accuracy.StatError (phot.flux.density;)",
			"Spectrum.Data.BackgroundModel.Accuracy.SysError (phot.flux.density;)",
			"Spectrum.Data.BackgroundModel.Quality (meta.code.qual;phot.flux.density,)",
			"Spectrum.Data.FluxAxis.Accuracy.StatErrHigh (phot.flux.density;)",
			"Spectrum.Data.FluxAxis.Accuracy.StatErrLow (phot.flux.density;)",
			"Spectrum.Data.FluxAxis.Accuracy.StatError (phot.flux.density;)",
			"Spectrum.Data.FluxAxis.Accuracy.SysError (phot.flux.density;)",
			"Spectrum.Data.FluxAxis.Quality (meta.code.qual;)",
			"Spectrum.DataID.Bandpass (instr.bandpass)",
			"Spectrum.DataID.CreatorDID (meta.id)",
			"Spectrum.DataID.DatasetID (meta.id;meta.dataset)",
			"Spectrum.DataID.Date (time;meta.dataset)",
			"Spectrum.DataID.Instrument (meta.id;instr)",
			"Spectrum.DataID.Logo (meta.ref.url)",
			"Spectrum.DataID.Title (meta.title;meta.dataset)",
			"Spectrum.DataID.Version (meta.version;meta.dataset)",
			"Spectrum.Data.SpectralAxis.Accuracy.BinHigh (em.*;stat.max)",
			"Spectrum.Data.SpectralAxis.Accuracy.BinLow (em.*;stat.min)",
			"Spectrum.Data.SpectralAxis.Accuracy.BinSize (em.*;spect.binSize)",
			"Spectrum.Data.SpectralAxis.Accuracy.StatErrHigh (em.*;)",
			"Spectrum.Data.SpectralAxis.Accuracy.StatErrLow (em.*;)",
			"Spectrum.Data.SpectralAxis.Accuracy.StatError (em.*;)",
			"Spectrum.Data.SpectralAxis.Accuracy.SysError (em.*;)",
			"Spectrum.Data.SpectralAxis.Resolution (spect.resolution;)",
			"Spectrum.Data.TimeAxis.Accuracy.BinHigh (time;stat.max)",
			"Spectrum.Data.TimeAxis.Accuracy.BinLow (time;stat.min)",
			"Spectrum.Data.TimeAxis.Accuracy.BinSize (time.interval)",
			"Spectrum.Data.TimeAxis.Accuracy.StatErrHigh (time;)",
			"Spectrum.Data.TimeAxis.Accuracy.StatErrLow (time;)",
			"Spectrum.Data.TimeAxis.Accuracy.StatError (time;)",
			"Spectrum.Data.TimeAxis.Accuracy.SysError (time;)",
			"Spectrum.Data.TimeAxis.Resolution (time.resolution)",
			"Spectrum.Derived.Redshift.StatError (stat.error;src.redshift)",
			"Spectrum.Derived.SNR (stat.snr)",
			"Spectrum.Derived.VarAmpl (src.var.amplitude;arith.ratio)",
			"Spectrum.Length (meta.number)",
			"Spectrum.Target.Class (src.class)",
			"Spectrum.Target.Description (meta.note;src)",
			"Spectrum.Target.Name (meta.id;src)",
			"Spectrum.Target.Pos (pos.eq;src)",
			"Spectrum.Target.Redshift (src.redshift)",
			"Spectrum.Target.SpectralClass (src.spType)",
			"Spectrum.Target.VarAmpl (src.var.amplitude)",
			"Spectrum.TimeSI (time;arith.zp)"};
	
	public VoSpectrumDMTree(Frame frame) {
		super(frame, "Spectrum utypes  (drag & drop to the meta-data panel)");
		this.flat_types = spectral_utypes;
	}

	/* (non-Javadoc)
	 * @see gui.VOTree#getPathComponents(java.lang.String)
	 */
	protected String[] getPathComponents(String string) {
		int pos = string.indexOf(' ');

		String[] without_ucd = string.substring(0, pos).split(" ");
		String utype = without_ucd[0];
		String[] utype_tokens = utype.split("\\.");
		//utype_tokens[utype_tokens.length - 1] = string;
		utype_tokens[utype_tokens.length - 1] = utype + " " + string.substring(pos);
		return utype_tokens;
	}
	
	protected void setDragFeatures() {
		this.tree.setDragEnabled(true);
	}


}
