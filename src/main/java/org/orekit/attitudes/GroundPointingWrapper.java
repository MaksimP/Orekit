/* Copyright 2002-2014 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.attitudes;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AngularDerivativesFilter;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.TimeStampedAngularCoordinates;


/** This class leverages common parts for compensation modes around ground pointing attitudes.
 * @author V&eacute;ronique Pommier-Maurussane
 * @author Luc Maisonobe
 */
public abstract class GroundPointingWrapper extends GroundPointing implements AttitudeProviderModifier {

    /** Serializable UID. */
    private static final long serialVersionUID = 262999520075931766L;

    /** Underlying ground pointing attitude provider.  */
    private final GroundPointing groundPointingLaw;

    /** Creates a new instance.
     * @param groundPointingLaw ground pointing attitude provider without compensation
     */
    public GroundPointingWrapper(final GroundPointing groundPointingLaw) {
        super(groundPointingLaw.getBodyFrame());
        this.groundPointingLaw = groundPointingLaw;
    }

    /** Get the underlying ground pointing law.
     * @return underlying ground pointing law.
     * @see #getUnderlyingAttitudeProvider()
     * @deprecated as of 6.0, replaced by {@link #getUnderlyingAttitudeProvider()}
     */
    @Deprecated
    public GroundPointing getGroundPointingLaw() {
        return groundPointingLaw;
    }

    /** Get the underlying (ground pointing) attitude provider.
     * @return underlying attitude provider, which in this case is a {@link GroundPointing} instance
     */
    public AttitudeProvider getUnderlyingAttitudeProvider() {
        return groundPointingLaw;
    }

    /** {@inheritDoc} */
    protected Vector3D getTargetPoint(final PVCoordinatesProvider pvProv,
                                      final AbsoluteDate date, final Frame frame)
        throws OrekitException {
        return groundPointingLaw.getTargetPoint(pvProv, date, frame);
    }

    /** {@inheritDoc} */
    @Override
    protected PVCoordinates getTargetPV(final PVCoordinatesProvider pvProv,
                                        final AbsoluteDate date, final Frame frame)
        throws OrekitException {
        return groundPointingLaw.getTargetPV(pvProv, date, frame);
    }

    /** Compute the base system state at given date, without compensation.
     * @param pvProv provider for PV coordinates
     * @param date date at which state is requested
     * @param frame reference frame from which attitude is computed
     * @return satellite base attitude state, i.e without compensation.
     * @throws OrekitException if some specific error occurs
     */
    public Attitude getBaseState(final PVCoordinatesProvider pvProv,
                                 final AbsoluteDate date, final Frame frame)
        throws OrekitException {
        return groundPointingLaw.getAttitude(pvProv, date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public Attitude getAttitude(final PVCoordinatesProvider pvProv,
                                final AbsoluteDate date, final Frame frame)
        throws OrekitException {

        // attitude from base attitude provider
        final Attitude base = getBaseState(pvProv, date, frame);

        // build a small sample for estimating derivatives
        final double h = 0.1;
        final List<TimeStampedAngularCoordinates> sample = new ArrayList<TimeStampedAngularCoordinates>();
        sample.add(order0Compensated(pvProv, base.shiftedBy(-h)));
        sample.add(order0Compensated(pvProv, base));
        sample.add(order0Compensated(pvProv, base.shiftedBy(+h)));

        // use interpolation to compute derivatives
        final TimeStampedAngularCoordinates interpolated =
                TimeStampedAngularCoordinates.interpolate(date, AngularDerivativesFilter.USE_R, sample);

        return new Attitude(frame, interpolated);

    }

    /** Compute the compensated attitude without derivatives.
     * @param pvProv provider for PV coordinates
     * @param base base attitude to compensate
     * @return compensated attitude
     * @throws OrekitException if some specific error occurs
     */
    private TimeStampedAngularCoordinates order0Compensated(final PVCoordinatesProvider pvProv, final Attitude base)
        throws OrekitException {
        final Rotation compensation = getCompensation(pvProv, base.getDate(), base.getReferenceFrame(), base);
        return new TimeStampedAngularCoordinates(base.getDate(),
                                                 compensation.applyTo(base.getRotation()),
                                                 Vector3D.ZERO, Vector3D.ZERO);
    }

    /** Compute the compensation rotation at given date.
     * @param pvProv provider for PV coordinates
     * @param date date at which rotation is requested
     * @param frame reference frame from which attitude is computed
     * @param base base satellite attitude in given frame.
     * @return compensation rotation at date, i.e rotation between non compensated
     * attitude state and compensated state.
     * @throws OrekitException if some specific error occurs
     */
    public abstract Rotation getCompensation(final PVCoordinatesProvider pvProv,
                                             final AbsoluteDate date, final Frame frame,
                                             final Attitude base)
        throws OrekitException;

}
