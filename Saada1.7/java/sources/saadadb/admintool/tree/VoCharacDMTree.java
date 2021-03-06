package saadadb.admintool.tree;

import java.awt.Frame;


/**
 * @author laurentmichel
 * * @version $Id: VoCharacDMTree.java 118 2012-01-06 14:33:51Z laurent.mistahl $

 */
public class VoCharacDMTree extends VoTree {
	String[] flat_utypes = {
			"ChAxis.AxisName (Axis name )",
			"ChAxis.calibrationStatus (Defines if and how the axis one is CALIBRATED)",
			"ChAxis.coordsystem (Reference coordinate system )",
			"ChAxis.independentaxis (Tells us if the axis is dependent)",
			"ChAxis.numBins (number of bins for this axis. array of 1 2 or 3 integers)",
			"ChAxis.ObsyLoc (Observatory location stc)",
			"ChAxis.regularsamplingStatus (a flag to tell if the data are regularly sampled.)",
			"ChAxis.ucd (Axis ucd: physical meaning standard UCD vocabulary )",
			"ChAxis.undersamplingStatus (a flag to tell if the data are undersampled.)",
			"ChAxis.unit (Default unit for the axis)",
			"ChAxis.accuracy (Global accuracy description of the axis)",
			"ChAxis.accuracy.Error (a generic error subtree with the flavor field)",
			"ChAxis.accuracy.quality (A combination of flags attesting the data quality )",
			"ChAxis.accuracy.statError (statistical error along the axis )",
			"ChAxis.accuracy.statError.ErrorBounds.documentation (A description of the Error value range.)",
			"ChAxis.accuracy.statError.ErrorBounds.ErrorLimits (Hi and Low values of statistical errors.)",
			"ChAxis.accuracy.statError.ErrorBounds (Error value range)",
			"ChAxis.accuracy.statError.ErrorRefVal.ErrorRefValue (Typical statistical error value  )",
			"ChAxis.accuracy.statError.ErrorRefVal (Typical statistical Error on the axis)",
			"ChAxis.accuracy.statError.ErrorVariability (A detailed description of the local error values )",
			"ChAxis.accuracy.statError.flavor (Type of the Error described)",
			"ChAxis.accuracy.sysError (systematic error along the axis )",
			"ChAxis.accuracy.sysError.ErrorBounds.documentation (A description of the Error value range.)",
			"ChAxis.accuracy.sysError.ErrorBounds.ErrorLimits (Hi and Low values of systematic errors.)",
			"ChAxis.accuracy.sysError.ErrorBounds (Error value range)",
			"ChAxis.accuracy.sysError.ErrorRefVal.ErrorRefValue (Typical systematic error value  )",
			"ChAxis.accuracy.sysError.ErrorRefVal (Typical systematic Error on the axis)",
			"ChAxis.accuracy.sysError.ErrorVariability (A detailed description of the local error values )",
			"ChAxis.accuracy.sysError.flavor (Type of the Error described)",
			"ChAxis.coverage.bounds (Limits of the observation on this axis.)",
			"ChAxis.coverage.bounds.coordsystem (for bounds coord system redefinition)",
			"ChAxis.coverage.bounds.documentation (fA document to mention how the bounds are defined)",
			"ChAxis.coverage.bounds.extent (fThe size of the region delimited by the bounds and centered on Location)",
			"ChAxis.coverage.bounds.limits (The actual values defining the bounds)",
			"ChAxis.coverage.bounds.limits.charBox.size2.PosAngle (The roll angle of a charc box)",
			"ChAxis.coverage.bounds.limits.charBox.size2 (the size of the box in 1, 2 or 3D)",
			"ChAxis.coverage.bounds.limits.charBox.value (the center or reference position of the box)",
			"ChAxis.coverage.bounds.limits.CoordScalarInterval.LoLimit",
			"ChAxis.coverage.bounds.limits.CoordScalarInterval.HiLimit", 
			"ChAxis.coverage.coordsystem (Redefinition of coordsystem)",
			"ChAxis.coverage.location (Typical coordinate value on this axis if no bounds)",
			"ChAxis.coverage.location.coordsystem (Redefinition of coordsystem)",
			"ChAxis.coverage.location.coord (The typical coordinate value. stc:astroCoordsType)",
			"ChAxis.coverage.location.coord.ScalarCoordinate.Value ",
			"ChAxis.coverage.location.documentation (Any kind of documentation on location metadata)",
			"ChAxis.coverage.location.unit (Redefinition of local unit)",
			"ChAxis.coverage.sensitivity (Encodes the variability of the response)",
			"ChAxis.coverage.sensitivity.coordsystem (Local redefinition if needed)",
			"ChAxis.coverage.sensitivity.documentation (Documents with sensitivity information)",
			"ChAxis.coverage.sensitivity.sensitivityMap (A map describes the variability of the response)",
			"ChAxis.coverage.sensitivity.unit (Local redefinition if needed)",
			"ChAxis.coverage.support (Describes the area where measurements are effectively present and interpretable)",
			"ChAxis.coverage.support.Area (Defines the effective covered region for this axis)",
			"ChAxis.coverage.support.AreaType (Gives the name of the region\'s shape)",
			"ChAxis.coverage.support.coordsystem (Local redefinition if needed )",
			"ChAxis.coverage.support.documentation (Some text about the Support metadata any URI type)",
			"ChAxis.coverage.support.Extent (The size of the effectively covered region)",
			"ChAxis.coverage.support.unit (Local redefinition if needed )",
			"ChAxis.coverage.unit (Redefinition of unit for coverage)",
			"ChAxis.resolution (Minimum size of an interpretable signal.)",
			"ChAxis.resolution.coordsystem (Coord system redefinition for whole resolution )",
			"ChAxis.resolution.resolutionBounds.coordsystem (Redefinition for resolution Bounds)",
			"ChAxis.resolution.resolutionBounds.documentation (Explains how this resolution has been estimated)",
			"ChAxis.resolution.resolutionBounds.resolutionLimits (High and low values of resolution )",
			"ChAxis.resolution.resolutionBounds.unit (Redefinition for resolution Bounds)",
			"ChAxis.resolution.resolutionRefVal (Resolution Reference value m if no bounds)",
			"ChAxis.resolution.resolutionRefVal.coordsystem (Redefinition for Resolution Reference value )",
			"ChAxis.resolution.resolutionRefVal.ReferenceValue (Typical resolution value)",
			"ChAxis.resolution.resolutionRefVal.unit (Redefinition for Resolution Reference value )",
			"ChAxis.resolution.resolutionSupport.coordsystem (Just for local redefinition )",
			"ChAxis.resolution.resolutionSupport.documentation (Documentation on the current piece of metadata.)",
			"ChAxis.resolution.resolutionSupport.resolutionLimits (Set of High an Low values for resolution ranges)",
			"ChAxis.resolution.resolutionSupport.unit (Just for local redefinition )",
			"ChAxis.resolution.resolutionVariability.coordsystem (Just for local redefinition )",
			"ChAxis.resolution.resolutionVariability.documentation (Explains how this resolution variability has been estimated)",
			"ChAxis.resolution.resolutionVariability.resolutionMap (This map describes the variability resolution)",
			"ChAxis.resolution.resolutionVariability.unit (Just for local redefinition )",
			"ChAxis.resolution.unit (Unit redefinition for whole resolution)",
			"ChAxis.samplingPrecision (How data have been sampled along this axis)",
			"ChAxis.samplingPrecision.coordsystem (Redefinition for samplingPrecision)",
			"ChAxis.samplingPrecision.samplingPrecisionBounds.coordsystem (Redefinition for samplingPrecision bounds)",
			"ChAxis.samplingPrecision.samplingPrecisionBounds.documentation (A place for some explanations about the samplingBounds )",
			"ChAxis.samplingPrecision.samplingPrecisionBounds.samplingExtentLimits (High and Low values of sampleExtent)",
			"ChAxis.samplingPrecision.samplingPrecisionBounds.samplingPeriodLimits (High and Low values of Sampling Period )",
			"ChAxis.samplingPrecision.samplingPrecisionBounds.unit (Redefinition for samplingPrecision bounds)",
			"ChAxis.samplingPrecision.samplingPrecisionRefVal (Typical values for sampling period and sample extent)",
			"ChAxis.samplingPrecision.samplingPrecisionRefVal.documentation (Explains how the sampling Precision)",
			"ChAxis.samplingPrecision.samplingPrecisionRefVal.fillFactor (Fill factor )",
			"ChAxis.samplingPrecision.samplingPrecisionRefVal.sampleExtent (Typical sample Extent value of the axis)",
			"ChAxis.samplingPrecision.samplingPrecisionRefVal.samplingPeriod (Typical sampling Period value of the axis )",
			"ChAxis.samplingPrecision.samplingPrecisionSupport.coordsystem (Redefinition for samplingPrecision Support)",
			"ChAxis.samplingPrecision.samplingPrecisionSupport.documentation (Explains how the Sampling Support was done)",
			"ChAxis.samplingPrecision.samplingPrecisionSupport.sampleExtentLimits (set of Hi and Low values of Sample Extent)",
			"ChAxis.samplingPrecision.samplingPrecisionSupport.samplingPeriodLimits (Set of High and Low values of Sampling Period )",
			"ChAxis.samplingPrecision.samplingPrecisionSupport.unit (Redefinition for samplingPrecision Support)",
			"ChAxis.samplingPrecision.samplingPrecisionVariability.coordsystem (Redefinition for sampling variability)",
			"ChAxis.samplingPrecision.samplingPrecisionVariability.documentation (Explains how the samplingPrecision)",
			"ChAxis.samplingPrecision.samplingPrecisionVariability.samplingPrecisionMap (variability map of the sampling )",
			"ChAxis.samplingPrecision.samplingPrecisionVariability.unit (Redefinition for sampling variability)",
			"ChAxis.samplingPrecision.unit (Redefinition for samplingPrecision)",
			"ChAxis.samplingPrecisionRefVal.coordsystem (Redefinition for samplingPrecision Reference Value )",
			"ChAxis.samplingPrecisionRefVal.unit (Redefinition for samplingPrecision Reference Value)",
			
			"SpatialAxis.AxisName (Axis name )",
			"SpatialAxis.calibrationStatus (Defines if and how the axis one is CALIBRATED)",
			"SpatialAxis.coordsystem (Reference coordinate system )",
			"SpatialAxis.independentaxis (Tells us if the axis is dependent)",
			"SpatialAxis.numBins (number of bins for this axis. array of 1 2 or 3 integers)",
			"SpatialAxis.ObsyLoc (Observatory location stc)",
			"SpatialAxis.regularsamplingStatus (a flag to tell if the data are regularly sampled.)",
			"SpatialAxis.ucd (Axis ucd: physical meaning standard UCD vocabulary )",
			"SpatialAxis.undersamplingStatus (a flag to tell if the data are undersampled.)",
			"SpatialAxis.unit (Default unit for the axis)",
			"SpatialAxis.accuracy (Global accuracy description of the axis)",
			"SpatialAxis.accuracy.Error (a generic error subtree with the flavor field)",
			"SpatialAxis.accuracy.quality (A combination of flags attesting the data quality )",
			"SpatialAxis.accuracy.statError.ErrorBounds.documentation (A description of the Error value range.)",
			"SpatialAxis.accuracy.statError.ErrorBounds.ErrorLimits (Hi and Low values of statistical errors.)",
			"SpatialAxis.accuracy.statError.ErrorBounds (Error value range)",
			"SpatialAxis.accuracy.statError.ErrorRefVal.ErrorRefValue (Typical statistical error value  )",
			"SpatialAxis.accuracy.statError.ErrorRefVal (Typical statistical Error on the axis)",
			"SpatialAxis.accuracy.statError.ErrorVariability (A detailed description of the local error values )",
			"SpatialAxis.accuracy.statError.flavor (Type of the Error described)",
			"SpatialAxis.accuracy.sysError.ErrorBounds.documentation (A description of the Error value range.)",
			"SpatialAxis.accuracy.sysError.ErrorBounds.ErrorLimits (Hi and Low values of systematic errors.)",
			"SpatialAxis.accuracy.sysError.ErrorBounds (Error value range)",
			"SpatialAxis.accuracy.sysError.ErrorRefVal.ErrorRefValue (Typical systematic error value  )",
			"SpatialAxis.accuracy.sysError.ErrorRefVal (Typical systematic Error on the axis)",
			"SpatialAxis.accuracy.sysError.ErrorVariability (A detailed description of the local error values )",
			"SpatialAxis.accuracy.sysError.flavor (Type of the Error described)",
			"SpatialAxis.coverage.bounds (Limits of the observation on this axis.)",
			"SpatialAxis.coverage.bounds.coordsystem (for bounds coord system redefinition)",
			"SpatialAxis.coverage.bounds.documentation (fA document to mention how the bounds are defined)",
			"SpatialAxis.coverage.bounds.extent (fThe size of the region delimited by the bounds and centered on Location)",
			"SpatialAxis.coverage.bounds.limits.charBox.size2.PosAngle (The roll angle of a charc box)",
			"SpatialAxis.coverage.bounds.limits.charBox.size2 (the size of the box in 1, 2 or 3D)",
			"SpatialAxis.coverage.bounds.limits.charBox.value (the center or reference position of the box)",
			"SpatialAxis.coverage.bounds.limits.Coord2VecInterval.LoLimit2Vec",
			"SpatialAxis.coverage.bounds.limits.Coord2VecInterval.HiLimit2Vec",
			"SpatialAxis.coverage.bounds.limits (The actual values defining the bounds)",
			"SpatialAxis.coverage.coordsystem (Redefinition of coordsystem)",
			"SpatialAxis.coverage.location (Typical coordinate value on this axis if no bounds)",
			"SpatialAxis.coverage.location.coordsystem (Redefinition of coordsystem)",
			"SpatialAxis.coverage.location.coord (The typical coordinate value. stc:astroCoordsType)",
			"SpatialAxis.coverage.location.coord.Position2D.value2.C1",
			"SpatialAxis.coverage.location.coord.Position2D.value2.C2",
			"SpatialAxis.coverage.location.documentation (Any kind of documentation on location metadata)",
			"SpatialAxis.coverage.location.unit (Redefinition of local unit)",
			"SpatialAxis.coverage.sensitivity (Encodes the variability of the response)",
			"SpatialAxis.coverage.sensitivity.coordsystem (Local redefinition if needed)",
			"SpatialAxis.coverage.sensitivity.documentation (Documents with sensitivity information)",
			"SpatialAxis.coverage.sensitivity.sensitivityMap (A map describes the variability of the response)",
			"SpatialAxis.coverage.sensitivity.unit (Local redefinition if needed)",
			"SpatialAxis.coverage.support (Describes the area where measurements are effectively present and interpretable)",
			"SpatialAxis.coverage.support.Area (Defines the effective covered region for this axis)",
			"SpatialAxis.coverage.support.AreaType (Gives the name of the region\'s shape)",
			"SpatialAxis.coverage.support.coordsystem (Local redefinition if needed )",
			"SpatialAxis.coverage.support.documentation (Some text about the Support metadata any URI type)",
			"SpatialAxis.coverage.support.Extent (The size of the effectively covered region)",
			"SpatialAxis.coverage.support.unit (Local redefinition if needed )",
			"SpatialAxis.coverage.unit (Redefinition of unit for coverage)",
			"SpatialAxis.resolution (Minimum size of an interpretable signal.)",
			"SpatialAxis.resolution.coordsystem (Coord system redefinition for whole resolution )",
			"SpatialAxis.resolution.resolutionBounds.coordsystem (Redefinition for resolution Bounds)",
			"SpatialAxis.resolution.resolutionBounds.documentation (Explains how this resolution has been estimated)",
			"SpatialAxis.resolution.resolutionBounds.resolutionLimits (High and low values of resolution )",
			"SpatialAxis.resolution.resolutionBounds.unit (Redefinition for resolution Bounds)",
			"SpatialAxis.resolution.resolutionRefVal (Resolution Reference value m if no bounds)",
			"SpatialAxis.resolution.resolutionRefVal.coordsystem (Redefinition for Resolution Reference value )",
			"SpatialAxis.resolution.resolutionRefVal.ReferenceValue (Typical resolution value)",
			"SpatialAxis.resolution.resolutionRefVal.unit (Redefinition for Resolution Reference value )",
			"SpatialAxis.resolution.resolutionSupport.coordsystem (Just for local redefinition )",
			"SpatialAxis.resolution.resolutionSupport.documentation (Documentation on the current piece of metadata.)",
			"SpatialAxis.resolution.resolutionSupport.resolutionLimits (Set of High an Low values for resolution ranges)",
			"SpatialAxis.resolution.resolutionSupport.unit (Just for local redefinition )",
			"SpatialAxis.resolution.resolutionVariability.coordsystem (Just for local redefinition )",
			"SpatialAxis.resolution.resolutionVariability.documentation (Explains how this resolution variability has been estimated)",
			"SpatialAxis.resolution.resolutionVariability.resolutionMap (This map describes the variability resolution)",
			"SpatialAxis.resolution.resolutionVariability.unit (Just for local redefinition )",
			"SpatialAxis.resolution.unit (Unit redefinition for whole resolution)",
			"SpatialAxis.samplingPrecision (How data have been sampled along this axis)",
			"SpatialAxis.samplingPrecision.coordsystem (Redefinition for samplingPrecision)",
			"SpatialAxis.samplingPrecision.samplingPrecisionBounds.coordsystem (Redefinition for samplingPrecision bounds)",
			"SpatialAxis.samplingPrecision.samplingPrecisionBounds.documentation (A place for some explanations about the samplingBounds )",
			"SpatialAxis.samplingPrecision.samplingPrecisionBounds.samplingExtentLimits (High and Low values of sampleExtent)",
			"SpatialAxis.samplingPrecision.samplingPrecisionBounds.samplingPeriodLimits (High and Low values of Sampling Period )",
			"SpatialAxis.samplingPrecision.samplingPrecisionBounds.unit (Redefinition for samplingPrecision bounds)",
			"SpatialAxis.samplingPrecision.samplingPrecisionRefVal (Typical values for sampling period and sample extent)",
			"SpatialAxis.samplingPrecision.samplingPrecisionRefVal.documentation (Explains how the sampling Precision)",
			"SpatialAxis.samplingPrecision.samplingPrecisionRefVal.fillFactor (Fill factor )",
			"SpatialAxis.samplingPrecision.samplingPrecisionRefVal.sampleExtent (Typical sample Extent value of the axis)",
			"SpatialAxis.samplingPrecision.samplingPrecisionRefVal.samplingPeriod (Typical sampling Period value of the axis )",
			"SpatialAxis.samplingPrecision.samplingPrecisionSupport.coordsystem (Redefinition for samplingPrecision Support)",
			"SpatialAxis.samplingPrecision.samplingPrecisionSupport.documentation (Explains how the Sampling Support was done)",
			"SpatialAxis.samplingPrecision.samplingPrecisionSupport.sampleExtentLimits (set of Hi and Low values of Sample Extent)",
			"SpatialAxis.samplingPrecision.samplingPrecisionSupport.samplingPeriodLimits (Set of High and Low values of Sampling Period )",
			"SpatialAxis.samplingPrecision.samplingPrecisionSupport.unit (Redefinition for samplingPrecision Support)",
			"SpatialAxis.samplingPrecision.samplingPrecisionVariability.coordsystem (Redefinition for sampling variability)",
			"SpatialAxis.samplingPrecision.samplingPrecisionVariability.documentation (Explains how the samplingPrecision)",
			"SpatialAxis.samplingPrecision.samplingPrecisionVariability.samplingPrecisionMap (variability map of the sampling )",
			"SpatialAxis.samplingPrecision.samplingPrecisionVariability.unit (Redefinition for sampling variability)",
			"SpatialAxis.samplingPrecision.unit (Redefinition for samplingPrecision)",
			"SpatialAxis.samplingPrecisionRefVal.coordsystem (Redefinition for samplingPrecision Reference Value )",
			"SpatialAxis.samplingPrecisionRefVal.unit (Redefinition for samplingPrecision Reference Value)",
			
			"SpectralAxis.AxisName (Axis name )",
			"SpectralAxis.calibrationStatus (Defines if and how the axis one is CALIBRATED)",
			"SpectralAxis.coordsystem (Reference coordinate system )",
			"SpectralAxis.independentaxis (Tells us if the axis is dependent)",
			"SpectralAxis.numBins (number of bins for this axis. array of 1 2 or 3 integers)",
			"SpectralAxis.ObsyLoc (Observatory location stc)",
			"SpectralAxis.regularsamplingStatus (a flag to tell if the data are regularly sampled.)",
			"SpectralAxis.ucd (Axis ucd: physical meaning standard UCD vocabulary )",
			"SpectralAxis.undersamplingStatus (a flag to tell if the data are undersampled.)",
			"SpectralAxis.unit (Default unit for the axis)",
			"SpectralAxis.accuracy (Global accuracy description of the axis)",
			"SpectralAxis.accuracy.Error (a generic error subtree with the flavor field)",
			"SpectralAxis.accuracy.quality (A combination of flags attesting the data quality )",
			"SpectralAxis.accuracy.statError.ErrorBounds.documentation (A description of the Error value range.)",
			"SpectralAxis.accuracy.statError.ErrorBounds.ErrorLimits (Hi and Low values of statistical errors.)",
			"SpectralAxis.accuracy.statError.ErrorBounds (Error value range)",
			"SpectralAxis.accuracy.statError.ErrorRefVal.ErrorRefValue (Typical statistical error value  )",
			"SpectralAxis.accuracy.statError.ErrorRefVal (Typical statistical Error on the axis)",
			"SpectralAxis.accuracy.statError.ErrorVariability (A detailed description of the local error values )",
			"SpectralAxis.accuracy.statError.flavor (Type of the Error described)",
			"SpectralAxis.accuracy.sysError.ErrorBounds.documentation (A description of the Error value range.)",
			"SpectralAxis.accuracy.sysError.ErrorBounds.ErrorLimits (Hi and Low values of systematic errors.)",
			"SpectralAxis.accuracy.sysError.ErrorBounds (Error value range)",
			"SpectralAxis.accuracy.sysError.ErrorRefVal.ErrorRefValue (Typical systematic error value  )",
			"SpectralAxis.accuracy.sysError.ErrorRefVal (Typical systematic Error on the axis)",
			"SpectralAxis.accuracy.sysError.ErrorVariability (A detailed description of the local error values )",
			"SpectralAxis.accuracy.sysError.flavor (Type of the Error described)",
			"SpectralAxis.coverage.bounds (Limits of the observation on this axis.)",
			"SpectralAxis.coverage.bounds.coordsystem (for bounds coord system redefinition)",
			"SpectralAxis.coverage.bounds.documentation (fA document to mention how the bounds are defined)",
			"SpectralAxis.coverage.bounds.extent (fThe size of the region delimited by the bounds and centered on Location)",
			"SpectralAxis.coverage.bounds.limits.charBox.size2.PosAngle (The roll angle of a charc box)",
			"SpectralAxis.coverage.bounds.limits.charBox.size2 (the size of the box in 1, 2 or 3D)",
			"SpectralAxis.coverage.bounds.limits.charBox.value (the center or reference position of the box)",
			"SpectralAxis.coverage.bounds.limits (The actual values defining the bounds)",
			"SpectralAxis.coverage.bounds.limits.CoordScalarInterval.LoLimit", 
			"SpectralAxis.coverage.bounds.limits.CoordScalarInterval.HiLimit", 
			"SpectralAxis.coverage.coordsystem (Redefinition of coordsystem)",
			"SpectralAxis.coverage.location (Typical coordinate value on this axis if no bounds)",
			"SpectralAxis.coverage.location.coordsystem (Redefinition of coordsystem)",
			"SpectralAxis.coverage.location.coord (The typical coordinate value. stc:astroCoordsType)",
			"SpectralAxis.coverage.location.coord.ScalarCoordinate.Value",
			"SpectralAxis.coverage.location.documentation (Any kind of documentation on location metadata)",
			"SpectralAxis.coverage.location.unit (Redefinition of local unit)",
			"SpectralAxis.coverage.sensitivity (Encodes the variability of the response)",
			"SpectralAxis.coverage.sensitivity.coordsystem (Local redefinition if needed)",
			"SpectralAxis.coverage.sensitivity.documentation (Documents with sensitivity information)",
			"SpectralAxis.coverage.sensitivity.sensitivityMap (A map describes the variability of the response)",
			"SpectralAxis.coverage.sensitivity.unit (Local redefinition if needed)",
			"SpectralAxis.coverage.support (Describes the area where measurements are effectively present and interpretable)",
			"SpectralAxis.coverage.support.Area (Defines the effective covered region for this axis)",
			"SpectralAxis.coverage.support.AreaType (Gives the name of the region\'s shape)",
			"SpectralAxis.coverage.support.coordsystem (Local redefinition if needed )",
			"SpectralAxis.coverage.support.documentation (Some text about the Support metadata any URI type)",
			"SpectralAxis.coverage.support.Extent (The size of the effectively covered region)",
			"SpectralAxis.coverage.support.unit (Local redefinition if needed )",
			"SpectralAxis.coverage.unit (Redefinition of unit for coverage)",
			"SpectralAxis.resolution (Minimum size of an interpretable signal.)",
			"SpectralAxis.resolution.coordsystem (Coord system redefinition for whole resolution )",
			"SpectralAxis.resolution.resolutionBounds.coordsystem (Redefinition for resolution Bounds)",
			"SpectralAxis.resolution.resolutionBounds.documentation (Explains how this resolution has been estimated)",
			"SpectralAxis.resolution.resolutionBounds.resolutionLimits (High and low values of resolution )",
			"SpectralAxis.resolution.resolutionBounds.unit (Redefinition for resolution Bounds)",
			"SpectralAxis.resolution.resolutionRefVal (Resolution Reference value m if no bounds)",
			"SpectralAxis.resolution.resolutionRefVal.coordsystem (Redefinition for Resolution Reference value )",
			"SpectralAxis.resolution.resolutionRefVal.ReferenceValue (Typical resolution value)",
			"SpectralAxis.resolution.resolutionRefVal.unit (Redefinition for Resolution Reference value )",
			"SpectralAxis.resolution.resolutionSupport.coordsystem (Just for local redefinition )",
			"SpectralAxis.resolution.resolutionSupport.documentation (Documentation on the current piece of metadata.)",
			"SpectralAxis.resolution.resolutionSupport.resolutionLimits (Set of High an Low values for resolution ranges)",
			"SpectralAxis.resolution.resolutionSupport.unit (Just for local redefinition )",
			"SpectralAxis.resolution.resolutionVariability.coordsystem (Just for local redefinition )",
			"SpectralAxis.resolution.resolutionVariability.documentation (Explains how this resolution variability has been estimated)",
			"SpectralAxis.resolution.resolutionVariability.resolutionMap (This map describes the variability resolution)",
			"SpectralAxis.resolution.resolutionVariability.unit (Just for local redefinition )",
			"SpectralAxis.resolution.unit (Unit redefinition for whole resolution)",
			"SpectralAxis.samplingPrecision (How data have been sampled along this axis)",
			"SpectralAxis.samplingPrecision.coordsystem (Redefinition for samplingPrecision)",
			"SpectralAxis.samplingPrecision.samplingPrecisionBounds.coordsystem (Redefinition for samplingPrecision bounds)",
			"SpectralAxis.samplingPrecision.samplingPrecisionBounds.documentation (A place for some explanations about the samplingBounds )",
			"SpectralAxis.samplingPrecision.samplingPrecisionBounds.samplingExtentLimits (High and Low values of sampleExtent)",
			"SpectralAxis.samplingPrecision.samplingPrecisionBounds.samplingPeriodLimits (High and Low values of Sampling Period )",
			"SpectralAxis.samplingPrecision.samplingPrecisionBounds.unit (Redefinition for samplingPrecision bounds)",
			"SpectralAxis.samplingPrecision.samplingPrecisionRefVal (Typical values for sampling period and sample extent)",
			"SpectralAxis.samplingPrecision.samplingPrecisionRefVal.documentation (Explains how the sampling Precision)",
			"SpectralAxis.samplingPrecision.samplingPrecisionRefVal.fillFactor (Fill factor )",
			"SpectralAxis.samplingPrecision.samplingPrecisionRefVal.sampleExtent (Typical sample Extent value of the axis)",
			"SpectralAxis.samplingPrecision.samplingPrecisionRefVal.samplingPeriod (Typical sampling Period value of the axis )",
			"SpectralAxis.samplingPrecision.samplingPrecisionSupport.coordsystem (Redefinition for samplingPrecision Support)",
			"SpectralAxis.samplingPrecision.samplingPrecisionSupport.documentation (Explains how the Sampling Support was done)",
			"SpectralAxis.samplingPrecision.samplingPrecisionSupport.sampleExtentLimits (set of Hi and Low values of Sample Extent)",
			"SpectralAxis.samplingPrecision.samplingPrecisionSupport.samplingPeriodLimits (Set of High and Low values of Sampling Period )",
			"SpectralAxis.samplingPrecision.samplingPrecisionSupport.unit (Redefinition for samplingPrecision Support)",
			"SpectralAxis.samplingPrecision.samplingPrecisionVariability.coordsystem (Redefinition for sampling variability)",
			"SpectralAxis.samplingPrecision.samplingPrecisionVariability.documentation (Explains how the samplingPrecision)",
			"SpectralAxis.samplingPrecision.samplingPrecisionVariability.samplingPrecisionMap (variability map of the sampling )",
			"SpectralAxis.samplingPrecision.samplingPrecisionVariability.unit (Redefinition for sampling variability)",
			"SpectralAxis.samplingPrecision.unit (Redefinition for samplingPrecision)",
			"SpectralAxis.samplingPrecisionRefVal.coordsystem (Redefinition for samplingPrecision Reference Value )",
			"SpectralAxis.samplingPrecisionRefVal.unit (Redefinition for samplingPrecision Reference Value)",
			
			"Time.Axis.AxisName (Axis name )",
			"Time.Axis.calibrationStatus (Defines if and how the axis one is CALIBRATED)",
			"Time.Axis.coverage.unit (Redefinition of unit for coverage)",
			"Time.Axis.independentaxis (Tells us if the axis is dependent)",
			"Time.Axis.numBins (number of bins for this axis. array of 1 2 or 3 integers)",
			"Time.Axis.ObsyLoc (Observatory location stc)",
			"Time.Axis.regularsamplingStatus (a flag to tell if the data are regularly sampled.)",
			"Time.Axis.ucd (Axis ucd: physical meaning standard UCD vocabulary )",
			"Time.Axis.undersamplingStatus (a flag to tell if the data are undersampled.)",
			"Time.Axis.unit (Default unit for the axis)",
			"Time.Axis.accuracy (Global accuracy description of the axis)",
			"Time.Axis.accuracy.Error (a generic error subtree with the flavor field)",
			"Time.Axis.accuracy.quality (A combination of flags attesting the data quality )",
			"Time.Axis.accuracy.statError.ErrorBounds.documentation (A description of the Error value range.)",
			"Time.Axis.accuracy.statError.ErrorBounds.ErrorLimits (Hi and Low values of statistical errors.)",
			"Time.Axis.accuracy.statError.ErrorBounds (Error value range)",
			"Time.Axis.accuracy.statError.ErrorRefVal.ErrorRefValue (Typical statistical error value  )",
			"Time.Axis.accuracy.statError.ErrorRefVal (Typical statistical Error on the axis)",
			"Time.Axis.accuracy.statError.ErrorVariability (A detailed description of the local error values )",
			"Time.Axis.accuracy.statError.flavor (Type of the Error described)",
			"Time.Axis.accuracy.sysError.ErrorBounds.documentation (A description of the Error value range.)",
			"Time.Axis.accuracy.sysError.ErrorBounds.ErrorLimits (Hi and Low values of systematic errors.)",
			"Time.Axis.accuracy.sysError.ErrorBounds (Error value range)",
			"Time.Axis.accuracy.sysError.ErrorRefVal.ErrorRefValue (Typical systematic error value  )",
			"Time.Axis.accuracy.sysError.ErrorRefVal (Typical systematic Error on the axis)",
			"Time.Axis.accuracy.sysError.ErrorVariability (A detailed description of the local error values )",
			"Time.Axis.accuracy.sysError.flavor (Type of the Error described)",
			"Time.Axis.coordsystem (Reference coordinate system )",
			"Time.Axis.coverage.bounds (Limits of the observation on this axis.)",
			"Time.Axis.coverage.bounds.coordsystem (for bounds coord system redefinition)",
			"Time.Axis.coverage.bounds.documentation (fA document to mention how the bounds are defined)",
			"Time.Axis.coverage.bounds.extent (fThe size of the region delimited by the bounds and centered on Location)",
			"Time.Axis.coverage.bounds.limits.charBox.size2.PosAngle (The roll angle of a charc box)",
			"Time.Axis.coverage.bounds.limits.charBox.size2 (the size of the box in 1, 2 or 3D)",
			"Time.Axis.coverage.bounds.limits.charBox.value (the center or reference position of the box)",
			"Time.Axis.coverage.bounds.limits (The actual values defining the bounds)",
			"Time.Axis.coverage.bounds.limits.TimeInterval.StartTime.MJDTime ()", 
			"Time.Axis.coverage.bounds.limits.TimeInterval.StartTime.ISOTime", 
			"Time.Axis.coverage.bounds.limits.TimeInterval.StopTime.MJDTime", 
			"Time.Axis.coverage.bounds.limits.TimeInterval.StopTime.ISOTime", 
			"Time.Axis.coverage.coordsystem (Redefinition of coordsystem)",
			"Time.Axis.coverage.location (Typical coordinate value on this axis if no bounds)",
			"Time.Axis.coverage.location.coordsystem (Redefinition of coordsystem)",
			"Time.Axis.coverage.location.coord (The typical coordinate value. stc:astroCoordsType)",
			"Time.Axis.coverage.location.coord.Time.TimeInstant.MJDTime", 
			"Time.Axis.coverage.location.coord.Time.TimeInstant.ISOTime", 
			"Time.Axis.coverage.location.documentation (Any kind of documentation on location metadata)",
			"Time.Axis.coverage.location.unit (Redefinition of local unit)",
			"Time.Axis.coverage.sensitivity (Encodes the variability of the response)",
			"Time.Axis.coverage.sensitivity.coordsystem (Local redefinition if needed)",
			"Time.Axis.coverage.sensitivity.documentation (Documents with sensitivity information)",
			"Time.Axis.coverage.sensitivity.sensitivityMap (A map describes the variability of the response)",
			"Time.Axis.coverage.sensitivity.unit (Local redefinition if needed)",
			"Time.Axis.coverage.support (Describes the area where measurements are effectively present and interpretable)",
			"Time.Axis.coverage.support.Area (Defines the effective covered region for this axis)",
			"Time.Axis.coverage.support.AreaType (Gives the name of the region shape)",
			"Time.Axis.coverage.support.coordsystem (Local redefinition if needed )",
			"Time.Axis.coverage.support.documentation (Some text about the Support metadata any URI type)",
			"Time.Axis.coverage.support.Extent (The size of the effectively covered region)",
			"Time.Axis.coverage.support.unit (Local redefinition if needed )",
			"Time.Axis.resolution (Minimum size of an interpretable signal.)",
			"Time.Axis.resolution.coordsystem (Coord system redefinition for whole resolution )",
			"Time.Axis.resolution.resolutionBounds.coordsystem (Redefinition for resolution Bounds)",
			"Time.Axis.resolution.resolutionBounds.documentation (Explains how this resolution has been estimated)",
			"Time.Axis.resolution.resolutionBounds.resolutionLimits (High and low values of resolution )",
			"Time.Axis.resolution.resolutionBounds.unit (Redefinition for resolution Bounds)",
			"Time.Axis.resolution.resolutionRefVal (Resolution Reference value m if no bounds)",
			"Time.Axis.resolution.resolutionRefVal.coordsystem (Redefinition for Resolution Reference value )",
			"Time.Axis.resolution.resolutionRefVal.ReferenceValue (Typical resolution value)",
			"Time.Axis.resolution.resolutionRefVal.unit (Redefinition for Resolution Reference value )",
			"Time.Axis.resolution.resolutionSupport.coordsystem (Just for local redefinition )",
			"Time.Axis.resolution.resolutionSupport.documentation (Documentation on the current piece of metadata.)",
			"Time.Axis.resolution.resolutionSupport.resolutionLimits (Set of High an Low values for resolution ranges)",
			"Time.Axis.resolution.resolutionSupport.unit (Just for local redefinition )",
			"Time.Axis.resolution.resolutionVariability.coordsystem (Just for local redefinition )",
			"Time.Axis.resolution.resolutionVariability.documentation (Explains how this resolution variability has been estimated)",
			"Time.Axis.resolution.resolutionVariability.resolutionMap (This map describes the variability resolution)",
			"Time.Axis.resolution.resolutionVariability.unit (Just for local redefinition )",
			"Time.Axis.resolution.unit (Unit redefinition for whole resolution)",
			"Time.Axis.samplingPrecision (How data have been sampled along this axis)",
			"Time.Axis.samplingPrecision.coordsystem (Redefinition for samplingPrecision)",
			"Time.Axis.samplingPrecision.samplingPrecisionBounds.coordsystem (Redefinition for samplingPrecision bounds)",
			"Time.Axis.samplingPrecision.samplingPrecisionBounds.documentation (A place for some explanations about the samplingBounds )",
			"Time.Axis.samplingPrecision.samplingPrecisionBounds.samplingExtentLimits (High and Low values of sampleExtent)",
			"Time.Axis.samplingPrecision.samplingPrecisionBounds.samplingPeriodLimits (High and Low values of Sampling Period )",
			"Time.Axis.samplingPrecision.samplingPrecisionBounds.unit (Redefinition for samplingPrecision bounds)",
			"Time.Axis.samplingPrecision.samplingPrecisionRefVal (Typical values for sampling period and sample extent)",
			"Time.Axis.samplingPrecision.samplingPrecisionRefVal.documentation (Explains how the sampling Precision)",
			"Time.Axis.samplingPrecision.samplingPrecisionRefVal.fillFactor (Fill factor )",
			"Time.Axis.samplingPrecision.samplingPrecisionRefVal.sampleExtent (Typical sample Extent value of the axis)",
			"Time.Axis.samplingPrecision.samplingPrecisionRefVal.samplingPeriod (Typical sampling Period value of the axis )",
			"Time.Axis.samplingPrecision.samplingPrecisionSupport.coordsystem (Redefinition for samplingPrecision Support)",
			"Time.Axis.samplingPrecision.samplingPrecisionSupport.documentation (Explains how the Sampling Support was done)",
			"Time.Axis.samplingPrecision.samplingPrecisionSupport.sampleExtentLimits (set of Hi and Low values of Sample Extent)",
			"Time.Axis.samplingPrecision.samplingPrecisionSupport.samplingPeriodLimits (Set of High and Low values of Sampling Period )",
			"Time.Axis.samplingPrecision.samplingPrecisionSupport.unit (Redefinition for samplingPrecision Support)",
			"Time.Axis.samplingPrecision.samplingPrecisionVariability.coordsystem (Redefinition for sampling variability)",
			"Time.Axis.samplingPrecision.samplingPrecisionVariability.documentation (Explains how the samplingPrecision)",
			"Time.Axis.samplingPrecision.samplingPrecisionVariability.samplingPrecisionMap (variability map of the sampling )",
			"Time.Axis.samplingPrecision.samplingPrecisionVariability.unit (Redefinition for sampling variability)",
			"Time.Axis.samplingPrecision.unit (Redefinition for samplingPrecision)",
			"Time.Axis.samplingPrecisionRefVal.coordsystem (Redefinition for samplingPrecision Reference Value )",
			"Time.Axis.samplingPrecisionRefVal.unit (Redefinition for samplingPrecision Reference Value)",

	};
	

	public VoCharacDMTree(Frame frame) {
		super(frame, "Charact utypes (drag & drop to the meta-data panel)");
		this.flat_types = flat_utypes;
	}
	/* (non-Javadoc)
	 * @see gui.VOTree#getPathComponents(java.lang.String)
	 */
	protected String[] getPathComponents(String string) {
		int pos = string.indexOf(' ');
		String[] without_ucd ;
		String comment;
		if( pos >= 0 ) {
			without_ucd = string.substring(0, pos).split(" ");
			comment = string.substring(pos);
		}
		else {
			without_ucd = string.split(" ");		
			comment = "";
		}
		String utype = without_ucd[0];
		String[] utype_tokens = utype.split("\\.");
		//utype_tokens[utype_tokens.length - 1] = string;
		utype_tokens[utype_tokens.length - 1] = utype + " " + comment;
		return utype_tokens;

	}
	
	protected void setDragFeatures() {
		this.tree.setDragEnabled(true);
	}
}
