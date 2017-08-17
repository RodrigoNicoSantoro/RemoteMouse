package rodrigoss.com.remotemouse;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;

    private ImageView trackPad;

    private MenuItem btnConnect;

    private float initialXCoord = 0;
    private float initialYCoord = 0;
    private float distanceToMoveX = 0;
    private float distanceToMoveY = 0;
    private boolean mouseMoved = false;

    private RemoteMouseClient client;

    private boolean isConnected = false;

    private static final String RIGHT_CLICK = "right click";
    private static final String LEFT_CLICK = "left click";
    private static final String SCROLL_UP = "scroll up";
    private static final String SCROLL_DOWN = "scroll down";

    private String scroll = "";

    Map commands = new HashMap<>();

    private String homeServerAddress = "192.168.0.20";

    ////////
    private int pointerCount = 0;

    private float mPrimStartTouchEventX = -1;
    private float mPrimStartTouchEventY = -1;
    private float mSecStartTouchEventX = -1;
    private float mSecStartTouchEventY = -1;

    private int mViewScaledTouchSlop = 0;
    ///////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViewVariables();
        setSupportActionBar(toolbar);
        setTouchListeners();

        client = new RemoteMouseClient();

        setSecurityPolicy();
    }

    private void initViewVariables() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        trackPad = (ImageView) findViewById(R.id.trackPad);
    }

    private boolean twoFingerTap = false;

    private void setTouchListeners() {
        trackPad.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = (event.getAction() & MotionEvent.ACTION_MASK);

                switch (action) {
                    case MotionEvent.ACTION_POINTER_DOWN:
                    case MotionEvent.ACTION_DOWN:
                        pointerCount++;
                        if (pointerCount == 1) {
                            mPrimStartTouchEventX = event.getX(0);
                            mPrimStartTouchEventY = event.getY(0);
                            initialXCoord = event.getX(0);
                            initialYCoord = event.getY(0);
                            mouseMoved = false;
                        }
                        if (pointerCount == 2) {
                            // Starting distance between fingers
                            mSecStartTouchEventX = event.getX(1);
                            mSecStartTouchEventY = event.getY(1);
                        }

                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        if (!mouseMoved) {
                            if (pointerCount == 2) {
                                client.sendCommand(RIGHT_CLICK);
                                Log.d("TAG", "2 finger tap");
                                twoFingerTap = true;
                            }
                            else if (pointerCount == 1 && twoFingerTap == false) {
                                client.sendCommand(LEFT_CLICK);
                                Log.d("TAG", "1 finger tap");
                            }
                        }
                        pointerCount--;
                        if (pointerCount == 0) {
                            twoFingerTap = false;
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:
                        boolean isPrimMoving = isScrollGesture(event, 0, mPrimStartTouchEventX, mPrimStartTouchEventY);
                        boolean isSecMoving = (pointerCount > 1 && isScrollGesture(event, 1, mSecStartTouchEventX, mSecStartTouchEventY));

                        if (isPrimMoving || isSecMoving) {
                            // A 1 finger or 2 finger scroll.
                            if (isPrimMoving && isSecMoving) {
                                /*
                                Log.d("Tag", "ORIGINAL X: " + originalX);
                                Log.d("Tag", "CURRENT X: " + event.getX(ptrIndex));
                                Log.d("Tag", "ORIGINAL Y: " + originalY);
                                Log.d("Tag", "CURRENT Y: " + event.getX(ptrIndex));*/
                                if(mSecStartTouchEventY < event.getY(1)) {
                                    scroll = SCROLL_DOWN;
                                }
                                else {
                                    scroll = SCROLL_UP;
                                }
                                Log.d("TAG", "Two finger scroll " + scroll);
                                scrollGestureCounter++;
                                if(scrollGestureCounter == 10) {
                                    client.sendCommand(scroll);
                                    scrollGestureCounter = 0;
                                }
                                mouseMoved = true;
                                mSecStartTouchEventX = event.getX(1);
                                mSecStartTouchEventY = event.getY(1);
                            } else {
                                if (notLeftClick(event)) {
                                    movePointer(event.getX(0), event.getY(0));
                                    Log.d("TAG", "One finger scroll");
                                }

                            }
                        }
                        break;
                }

                return true;
            }
        });
    }

    private int scrollGestureCounter = 0;

    private boolean notLeftClick(MotionEvent event) {
        return initialXCoord - event.getX() > 5 || initialXCoord - event.getX() < -5 || initialYCoord - event.getY() > 5 || initialYCoord - event.getY() < -5;
    }

    private void movePointer(float currentX, float currentY) {
        distanceToMoveX = currentX - initialXCoord;
        distanceToMoveY = currentY - initialYCoord;
        initialXCoord = currentX;
        initialYCoord = currentY;
        Log.d("TAG", String.valueOf(distanceToMoveX));
//        if (distanceToMoveX != 0 || distanceToMoveY != 0)
        client.sendCommand(distanceToMoveX + "," + distanceToMoveY); //send mouse movement to server as a String "xCoord,yCoord".
        mouseMoved = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        btnConnect = menu.findItem(R.id.action_connect);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            Log.d("menuTag", "settings clicked");
            return true;
        }*/
        if (id == R.id.action_connect) {
            Log.d("menuTag", "connect clicked");
            if (isConnected) {
                disconnectFromServer();
            } else {
                getIpAndConnectToServer();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void disconnectFromServer() {
        try {
            client.disconnectFromServer();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isConnected = false;
            btnConnect.setTitle("Connect");
            //trackPad.setText("Disconnected");
        }
    }

    /*@Override
    public void onClick(View v) {
        try {
            client.sendCommand(commands.get(v.getId()).toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private void setSecurityPolicy() {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void getIpAndConnectToServer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server Address");
        final EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        inputField.setText("192.168.0.");
        builder.setView(inputField);
        builder.setPositiveButton("Connect", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                connectToServer(inputField.getText().toString());
            }
        });

        builder.show();
    }

    private void connectToServer(String ipAddress) {
//            serverAddress = txtIP.getText().toString();
//            if(!serverAddress.isEmpty())
//        client.connectToServer(homeServerAddress);
        try {
            client.connectToServer(ipAddress);
            isConnected = true;
            btnConnect.setTitle("Disconnect");
            //trackPad.setText("Connected");
        } catch (IOException e) {
            e.printStackTrace();
            isConnected = false;
            btnConnect.setTitle("Connect");
            displayErrorDialog();
        }

    }

    private void displayErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connection failed.");
        final EditText inputField = new EditText(this);
        inputField.setInputType(InputType.TYPE_CLASS_TEXT);
        inputField.setText("Error connecting to server.");
        builder.setPositiveButton("Try again", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getIpAndConnectToServer();
            }
        });
        builder.show();
    }

    //////////////////////////////
    private boolean isScrollGesture(MotionEvent event, int ptrIndex, float originalX, float originalY) {
        float moveX = Math.abs(event.getX(ptrIndex) - originalX);
        float moveY = Math.abs(event.getY(ptrIndex) - originalY);

        if (moveX > mViewScaledTouchSlop || moveY > mViewScaledTouchSlop) {
            return true;
        }
        return false;
    }

    private float distance(MotionEvent event, int first, int second) {
        if (event.getPointerCount() >= 2) {
            final float x = event.getX(first) - event.getX(second);
            final float y = event.getY(first) - event.getY(second);

            return (float) Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }
}
