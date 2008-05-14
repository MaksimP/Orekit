package fr.cs.orekit.bodies;


import org.apache.commons.math.geometry.Vector3D;
import org.apache.commons.math.util.MathUtils;

import fr.cs.orekit.Utils;
import fr.cs.orekit.errors.OrekitException;
import fr.cs.orekit.frames.Frame;
import fr.cs.orekit.orbits.CircularParameters;
import fr.cs.orekit.time.AbsoluteDate;
import fr.cs.orekit.time.ChunkedDate;
import fr.cs.orekit.time.ChunkedTime;
import fr.cs.orekit.time.UTCScale;
import fr.cs.orekit.utils.Line;
import fr.cs.orekit.utils.PVCoordinates;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class OneAxisEllipsoidTest extends TestCase {

    public OneAxisEllipsoidTest(String name) {
        super(name);
    }

    public void testOrigin() throws OrekitException {
        double ae = 6378137.0;
        checkCartesianToEllipsoidic(ae, 1.0 / 298.257222101,
                                    ae, 0, 0,
                                    0, 0, 0);
    }

    public void testStandard() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257222101,
                                    4637885.347, 121344.608, 4362452.869,
                                    0.026157811533131, 0.757987116290729, 260.455572965555);
    }

    public void testLongitudeZero() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257222101,
                                    6378400.0, 0, 6379000.0,
                                    0.0, 0.787815771252351, 2653416.77864152);
    }

    public void testLongitudePi() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257222101,
                                    -6379999.0, 0, 6379000.0,
                                    3.14159265358979, 0.787690146758403, 2654544.7767725);
    }

    public void testNorthPole() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257222101,
                                    0.0, 0.0, 7000000.0,
                                    0.0, 1.57079632679490, 643247.685859644);
    }

    public void testEquator() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257222101,
                                    6379888.0, 6377000.0, 0.0,
                                    0.785171775899913, 0.0, 2642345.24279301);
    }

    public void testInside3Roots() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257,
                                    9219.0, -5322.0, 6056743.0,
                                    5.75963470503781, 1.56905114598949, -300000.009586231);
    }

    public void testInsideLessThan3Roots() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257,
                                    1366863.0, -789159.0, -5848.988,
                                    -0.523598928689, -0.00380885831963, -4799808.27951);
    }

    public void testOutside() throws OrekitException {
        checkCartesianToEllipsoidic(6378137.0, 1.0 / 298.257,
                                    5722966.0, -3304156.0, -24621187.0,
                                    5.75958652642615, -1.3089969725151, 19134410.3342696);
    }

    public void testGeoCar() throws OrekitException {
        OneAxisEllipsoid model =
            new OneAxisEllipsoid(6378137.0, 1.0 / 298.257222101,
                                 Frame.getReferenceFrame(Frame.ITRF2000B,
                                                         AbsoluteDate.J2000_EPOCH));
        GeodeticPoint nsp =
            new GeodeticPoint(0.0423149994747243, 0.852479154923577, 111.6);
        Vector3D p = model.transform(nsp);
        assertEquals(4201866.69291890, p.getX(), 1.0e-6);
        assertEquals(177908.184625686, p.getY(), 1.0e-6);
        assertEquals(4779203.64408617, p.getZ(), 1.0e-6);
    }

    public void testLineIntersection() throws OrekitException {
        AbsoluteDate date = AbsoluteDate.J2000_EPOCH;
        Frame frame = Frame.getReferenceFrame(Frame.ITRF2000B, date);    
        
        OneAxisEllipsoid model = new OneAxisEllipsoid(100.0, 0.9, frame);
        Vector3D point         = new Vector3D(0.0, 93.7139699, 3.5930796);
        Vector3D direction     = new Vector3D(0.0, 1.0, 1.0);
        Line line = new Line(point, direction);
        GeodeticPoint gp = model.getIntersectionPoint(line, point, frame, date);
        assertEquals(gp.altitude, 0.0, 1.0e-12);
        assertTrue(line.contains(model.transform(gp)));

        model = new OneAxisEllipsoid(100.0, 0.9, frame);
        point = new Vector3D(0.0, -93.7139699, -3.5930796);
        direction = new Vector3D(0.0, -1.0, -1.0);
        line = new Line(point, direction);
        gp = model.getIntersectionPoint(line, point, frame, date);
        assertTrue(line.contains(model.transform(gp)));

        model = new OneAxisEllipsoid(100.0, 0.9, frame);
        point = new Vector3D(0.0, -93.7139699, 3.5930796);
        direction = new Vector3D(0.0, -1.0, 1.0);
        line = new Line(point, direction);
        gp = model.getIntersectionPoint(line, point, frame, date);
        assertTrue(line.contains(model.transform(gp)));

        model = new OneAxisEllipsoid(100.0, 0.9, frame);
        point = new Vector3D(-93.7139699, 0.0, 3.5930796);
        direction = new Vector3D(-1.0, 0.0, 1.0);
        line = new Line(point, direction);
        gp = model.getIntersectionPoint(line, point, frame, date);
        assertTrue(line.contains(model.transform(gp)));

        point = new Vector3D(0.0, 0.0, 110);
        direction = new Vector3D(0.0, 0.0, 1.0);
        line = new Line(point, direction);
        gp = model.getIntersectionPoint(line, point, frame, date);
        assertEquals(gp.latitude, Math.PI/2, 1.0e-12);
        
        point = new Vector3D(0.0, 110, 0);
        direction = new Vector3D(0.0, 1.0, 0.0);
        line = new Line(point, direction);
        gp = model.getIntersectionPoint(line, point, frame, date);
        assertEquals(gp.latitude,0, 1.0e-12);

    }

    public void testNoLineIntersection() throws OrekitException {
        AbsoluteDate date = AbsoluteDate.J2000_EPOCH;
        Frame frame = Frame.getReferenceFrame(Frame.ITRF2000B, date);    
        OneAxisEllipsoid model = new OneAxisEllipsoid(100.0, 0.9, frame);
        Vector3D point     = new Vector3D(0.0, 93.7139699, 3.5930796);
        Vector3D direction = new Vector3D(0.0, 9.0, -2.0);
        Line line = new Line(point, direction);
        assertNull(model.getIntersectionPoint(line, point, frame, date));
    }

    public void testIntersectionFromPoints() throws OrekitException {
        AbsoluteDate date = new AbsoluteDate(new ChunkedDate(2008, 03, 21),
                                             ChunkedTime.H12,
                                             UTCScale.getInstance());
        
        Frame frame = Frame.getReferenceFrame(Frame.ITRF2000B, date); 
        OneAxisEllipsoid earth = new OneAxisEllipsoid(6378136.460, 1 / 298.257222101, frame);
        
        // Satellite on polar position
        // ***************************
        final double mu = 3.9860047e14;
        CircularParameters circ =
            new CircularParameters(7178000.0, 0.5e-4, 0., Math.toRadians(90.), Math.toRadians(60.),
                                   Math.toRadians(90.), CircularParameters.MEAN_LONGITUDE_ARGUMENT, Frame.getJ2000());
        System.out.println("POLAR");
      
        // Transform satellite position to position/velocity parameters in J2000 and ITRF200B
        PVCoordinates pvSatJ2000 = circ.getPVCoordinates(mu);
        PVCoordinates pvSatItrf  = frame.getTransformTo(Frame.getJ2000(), date).transformPVCoordinates(pvSatJ2000);
        Vector3D pSatItrf  = pvSatItrf.getPosition();
        
        // Test first visible surface points
        GeodeticPoint geoPoint = new GeodeticPoint(Math.toRadians(60.), Math.toRadians(70.), 0.);
        Vector3D pointItrf     = earth.transform(geoPoint);
        Vector3D direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        Line line = new Line(pSatItrf, direction);
        GeodeticPoint geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // Test second visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(-120.), Math.toRadians(65.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // Test non visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(60.), Math.toRadians(30.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        
        // For polar satellite position, intersection point is at the same longitude but different latitude
        assertEquals(Math.toRadians(59.83813849072837), geoInter.longitude, Utils.epsilonAngle);
        assertEquals(Math.toRadians(78.0357178015876), geoInter.latitude, Utils.epsilonAngle);
        
        // Satellite on equatorial position
        // ********************************
        circ =
            new CircularParameters(7178000.0, 0.5e-4, 0., Math.toRadians(1.e-4), Math.toRadians(0.),
                                   Math.toRadians(0.), CircularParameters.MEAN_LONGITUDE_ARGUMENT, Frame.getJ2000());
        System.out.println("EQUATORIAL");
      
        // Transform satellite position to position/velocity parameters in J2000 and ITRF200B
        pvSatJ2000 = circ.getPVCoordinates(mu);
        pvSatItrf  = frame.getTransformTo(Frame.getJ2000(), date).transformPVCoordinates(pvSatJ2000);
        pSatItrf  = pvSatItrf.getPosition();
        
        // Test first visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(0.), Math.toRadians(5.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        System.out.println("Sat point = " + pSatItrf.getX()
                                    + " " + pSatItrf.getY() 
                                    + " " + pSatItrf.getZ());
        System.out.println("Point = " + pointItrf.getX()
                           + " " + pointItrf.getY() 
                           + " " + pointItrf.getZ());
        System.out.println("Direction = " + direction.getX()
                                          + " " + direction.getY() 
                                          + " " + direction.getZ());
        System.out.println("Line direction = " + line.getDirection().getX()
                           + " " + line.getDirection().getY() 
                           + " " + line.getDirection().getZ());
        System.out.println("Sat abscissa = " + line.getAbscissa(pSatItrf));
        assertTrue(line.getAbscissa(pSatItrf) > 0);
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // With the point opposite to satellite point along the line
        GeodeticPoint geoInter2 = earth.getIntersectionPoint(line, line.pointAt(-line.getAbscissa(pSatItrf)), frame, date);
        System.out.println("Opposite intersection : lon = " + Math.toDegrees(geoInter2.longitude)
                                                + " lat = " + Math.toDegrees(geoInter2.latitude));
        assertTrue(Math.abs(geoInter.longitude - geoInter2.longitude) > Math.toRadians(0.1));
        assertTrue(Math.abs(geoInter.latitude - geoInter2.latitude) > Math.toRadians(0.1));
        
        // Test second visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(0.), Math.toRadians(-5.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // Test non visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(0.), Math.toRadians(40.), 0.);
        System.out.println("Point donne : lon = " + Math.toDegrees(geoPoint.longitude) 
                           + " lat = " + Math.toDegrees(geoPoint.latitude) 
                           + " alt = " + geoPoint.altitude); 
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        System.out.println("Point intersection : lon = " + Math.toDegrees(geoInter.longitude) 
                           + " lat = " + Math.toDegrees(geoInter.latitude) 
                           + " alt = " + geoInter.altitude); 
        System.out.println();
        
//        assertEquals(Math.toRadians(10.424082030386236), geoInter.longitude, Utils.epsilonAngle);
//        assertEquals(Math.toRadians(17.492951473090244), geoInter.latitude, Utils.epsilonAngle);


        // Satellite on any position
        // *************************
        circ =
            new CircularParameters(7178000.0, 0.5e-4, 0., Math.toRadians(50.), Math.toRadians(0.),
                                   Math.toRadians(90.), CircularParameters.MEAN_LONGITUDE_ARGUMENT, Frame.getJ2000());
        System.out.println("ANY");
        
        // Transform satellite position to position/velocity parameters in J2000 and ITRF200B
        pvSatJ2000 = circ.getPVCoordinates(mu);
        pvSatItrf  = frame.getTransformTo(Frame.getJ2000(), date).transformPVCoordinates(pvSatJ2000);
        pSatItrf  = pvSatItrf.getPosition();
        
        // Test first visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(90.), Math.toRadians(40.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // Test second visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(90.), Math.toRadians(60.), 0.);
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        assertEquals(geoPoint.longitude, geoInter.longitude, Utils.epsilonAngle);
        assertEquals(geoPoint.latitude, geoInter.latitude, Utils.epsilonAngle);
        
        // Test non visible surface points
        geoPoint = new GeodeticPoint(Math.toRadians(90.), Math.toRadians(0.), 0.);
        System.out.println("Point donne : lon = " + Math.toDegrees(geoPoint.longitude) 
                                      + " lat = " + Math.toDegrees(geoPoint.latitude) 
                                      + " alt = " + geoPoint.altitude); 
        pointItrf     = earth.transform(geoPoint);
        direction = new Vector3D(1., pSatItrf, -1., pointItrf);
        line = new Line(pSatItrf, direction);
        
        geoInter = earth.getIntersectionPoint(line, pSatItrf, frame, date);
        System.out.println("Point intersection : lon = " + Math.toDegrees(geoInter.longitude) 
                           + " lat = " + Math.toDegrees(geoInter.latitude) 
                           + " alt = " + geoInter.altitude); 
        System.out.println();
        
//        assertEquals(Math.toRadians(10.424082030386236), geoInter.longitude, Utils.epsilonAngle);
//        assertEquals(Math.toRadians(17.492951473090244), geoInter.latitude, Utils.epsilonAngle);


    }

    private void checkCartesianToEllipsoidic(double ae, double f,
                                             double x, double y, double z,
                                             double longitude, double latitude,
                                             double altitude)
        throws OrekitException {

        AbsoluteDate date = AbsoluteDate.J2000_EPOCH;
        Frame frame = Frame.getReferenceFrame(Frame.ITRF2000B, date);    
        OneAxisEllipsoid model = new OneAxisEllipsoid(ae, f, frame);
        GeodeticPoint gp = model.transform(new Vector3D(x, y, z), frame, date);
        assertEquals(longitude, MathUtils.normalizeAngle(gp.longitude, longitude), 1.0e-10);
        assertEquals(latitude,  gp.latitude,  1.0e-10);
        assertEquals(altitude,  gp.altitude,  1.0e-10 * Math.abs(altitude));
    }

    public static Test suite() {
        return new TestSuite(OneAxisEllipsoidTest.class);
    }

}

