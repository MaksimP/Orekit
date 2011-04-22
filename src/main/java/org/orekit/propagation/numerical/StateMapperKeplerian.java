package org.orekit.propagation.numerical;

import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;

/** Implementation of the {@link StateMapper} interface for state arrays in Keplerian parameters.
 * <p>
 * Instances of this class are guaranteed to be immutable
 * </p>
 *
 * @see org.orekit.propagation.SpacecraftState
 * @see org.orekit.propagation.numerical.NumericalPropagator
 * @see TimeDerivativesEquationsKeplerian
 * @author Luc Maisonobe
 */
public class StateMapperKeplerian implements StateMapper {

    /** Serializable UID. */
    private static final long serialVersionUID = 7999740018635724872L;

    /** Position angle type. */
    private final PositionAngle type;

    /** Attitude provider. */
    private final AttitudeProvider attitudeProvider;

    /** Create a new instance.
     * @param type position angle type
     * @param attitudeProvider attitude provider
     */
    public StateMapperKeplerian(final PositionAngle type, final AttitudeProvider provider) {
        this.type             = type;
        this.attitudeProvider = provider;
    }

    /** {@inheritDoc} */
    public void mapStateToArray(final SpacecraftState s, final double[] stateVector) {

        final KeplerianOrbit keplerianOrbit =
            (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(s.getOrbit());

        stateVector[0] = keplerianOrbit.getA();
        stateVector[1] = keplerianOrbit.getE();
        stateVector[2] = keplerianOrbit.getI();
        stateVector[3] = keplerianOrbit.getPerigeeArgument();
        stateVector[4] = keplerianOrbit.getRightAscensionOfAscendingNode();
        stateVector[5] = keplerianOrbit.getAnomaly(type);
        stateVector[6] = s.getMass();

    }

    /** {@inheritDoc} */
    public SpacecraftState mapArrayToState(final double[] stateVector, final AbsoluteDate date,
                                           final double mu, final Frame frame) throws OrekitException {
        final KeplerianOrbit orbit =
            new KeplerianOrbit(stateVector[0], stateVector[1], stateVector[2], stateVector[3],
                               stateVector[4], stateVector[5], type,
                               frame, date, mu);

        final Attitude attitude = attitudeProvider.getAttitude(orbit, date, frame);

        return new SpacecraftState(orbit, attitude, stateVector[6]);

    }

}