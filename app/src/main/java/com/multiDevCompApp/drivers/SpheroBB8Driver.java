
         package com.multiDevCompApp.drivers;

         import android.util.Log;
         import android.widget.Toast;

         import com.multiDevCompApp.MainActivity;
         import com.orbotix.ConvenienceRobot;
         import com.orbotix.Sphero;
         import com.orbotix.common.DiscoveryAgentEventListener;
         import com.orbotix.common.DiscoveryException;
         import com.orbotix.common.Robot;
         import com.orbotix.common.RobotChangedStateListener;
         import com.orbotix.le.DiscoveryAgentLE;
         import com.orbotix.le.RobotRadioDescriptor;

         import java.util.List;

public class SpheroBB8Driver implements RobotChangedStateListener, com.multiDevCompApp.drivers.interfaces.Robot {
    private MainActivity mainActivity;
    private DiscoveryAgentLE discoveryAgent;
    private static ConvenienceRobot robot;
    private DiscoveryAgentEventListener discoveryAgentEventListener = new DiscoveryAgentEventListener() {
        @Override
        public void handleRobotsAvailable(List<Robot> robots) {
            mainActivity.writeScreenLog("Robot found! Bring near the device to connect it");
        }
    };
    private RobotChangedStateListener robotStateListener = new RobotChangedStateListener() {
        @Override
        public void handleRobotChangedState(Robot r, RobotChangedStateNotificationType robotChangedStateNotificationType) {
            switch (robotChangedStateNotificationType) {
                case Online:
                    online(r);
                    break;
                case Offline:
                    offline(r);
                    break;
                case Connecting:
                    connecting(r);
                    break;
                case Connected:
                    connected(r);
                    break;
                case Disconnected:
                    disconnected(r);
                    break;
                case FailedConnect:
                    failedConnect(r);
                    break;
            }
        }
    };

    public SpheroBB8Driver(MainActivity ma){
        this.mainActivity = ma;
        DiscoveryAgentLE.getInstance().addRobotStateListener(this);
        connect();
    }

    //Ricerca bluetooth
    private void stopDiscovery() {
        // When a robot is connected, this is a good time to stop discovery. Discovery takes a lot of system
        // resources, and if left running, will cause your app to eat the user's battery up, and may cause
        // your application to run slowly. To do this, use DiscoveryAgent#stopDiscovery().
        discoveryAgent.stopDiscovery();

        // It is also proper form to not allow yourself to re-register for the discovery listeners, so let's
        // unregister for the available notifications here using DiscoveryAgent#removeDiscoveryListener().
        discoveryAgent.removeDiscoveryListener(discoveryAgentEventListener);
        discoveryAgent.removeRobotStateListener(robotStateListener);
        discoveryAgent = null;
    }
    void connect() {
        try {
            discoveryAgent = DiscoveryAgentLE.getInstance();

            // DiscoveryAgentLE serve a mandare una notifica appena trova un robot.
            // Per fare ciò ha bisogno di un elenco di handler degli eventi, fornitigli
            // dall'implementazione del metodo handleRobotsAvailable nella classe DiscoveryAgentEventListener
            discoveryAgent.addDiscoveryListener(discoveryAgentEventListener);

            // Allo stesso modo settiamo l'handler per il cambiamento di stato del robot
            discoveryAgent.addRobotStateListener(robotStateListener);

            // Creating a new radio descriptor to be able to connect to the BB8 robots
            RobotRadioDescriptor robotRadioDescriptor = new RobotRadioDescriptor();
            robotRadioDescriptor.setNamePrefixes(new String[]{"BB-"});
            discoveryAgent.setRadioDescriptor(robotRadioDescriptor);

            // Then to start looking for a BB8, you use DiscoveryAgent#connect()
            // You do need to handle the discovery exception. This can occur in cases where the user has
            // Bluetooth off, or when the discovery cannot be started for some other reason.
            discoveryAgent.startDiscovery(mainActivity.getApplicationContext());
        } catch (DiscoveryException e) {
            Log.e("Sphero", "Discovery Error: " + e);
            e.printStackTrace();
        }
    }

    //Led del robot
    void setRobotLedRed(){
        robot.setLed(1,0,0);
    }
    void setRobotLedGreen(){
        robot.setLed(0,1,0);
    }
    void setRobotLedBlue(){
        robot.setLed(0,0,1);
    }
    void setRobotLedYellow() {
        robot.setLed(1,1,0);
    }

    //Movimento
    void moveForward(double rotation, double velocity ){
        robot.drive((float) rotation, (float) velocity);
    }
    void stopRobot(){
        //robot.stop();
        robot.drive(0, 0);
    }

    //get-set
    static ConvenienceRobot getRobot(){
        return robot;
    }

    //cambi di stato del robot
    private void online(Robot r){
        stopDiscovery();
        mainActivity.debug(2,"Robot " + r.getName() + " is Online!");
        robot = new Sphero(r);
        robot.setLed(0, 1, 0);

    }
    private void offline(Robot r){
        connect();
    }
    private void connecting(Robot r){
    }
    private void connected(Robot r){
    }
    private void disconnected(Robot r){
        connect();
    }
    private void failedConnect(Robot r){
        connect();
    }

    void startCalibrating(){
        robot.calibrating(true);
    }
    void stopClaibrating(){
        robot.calibrating(false);
    }
    @Override
    public void handleRobotChangedState(Robot robot, RobotChangedStateNotificationType robotChangedStateNotificationType) {
    }

    @Override
    public void forward() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void turnL() {

    }

    @Override
    public void turnR() {

    }

    @Override
    public void ledOn(float red, float green, float blue) {
        robot.setLed(red, green, blue);
    }

    @Override
    public void ledOff() {
        robot.setLed(0,0,0);
    }

    @Override
    public boolean connect(int index) {
        return false;
    }

    @Override
    public boolean disconnect() {
        return false;
    }
}
