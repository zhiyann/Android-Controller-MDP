package com.example.mdp.group16controller;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class screenMain extends AppCompatActivity implements MapGridView.Listener {

    public static boolean robotHeadPosition = false;
    public static boolean robotWayPoint = false;

    private Handler handler;

    //Intent request code
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_ENABLE_BT = 3;
    private static final int REQUEST_PERSISTENT_DATA = 4;
    private static final String TAG = "screenMain";

    //grid value
    private List<String> gridValues = new ArrayList<>();
    private String binaryValues;
    private boolean firstTime = true;

    //robot on grid value
    private String robotHeadX;
    private String robotHeadY;
    private String robotHeadFacing;

    // Layout Views
    public static EditText robotCoordinate, robotWayPointCoordinate;
    private ImageButton forwardButton, reverseButton, rightButton, leftButton, rotateRightButton, rotateLeftButton, uTurn;
    private Button searchButton, configureButton, startExplore, startShortest, deleteAllObstacle, setRobotPosition, updateMap, deleteRobot, setWayPoint, deleteWayPoint;
    private TextView robotStatus;
    private Switch mapUpdateStatus;
    private EditText CoordinateX, CoordinateY;

    //Coordinate Robot
    int x = -1, y = -1;

    // Name of the connected device
    private String mConnectedDeviceName = null;

    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;

    //Member object for the chat services
    private BluetoothService mChatService = null;

    BluetoothAdapter mBluetoothAdapter;
    Toast toast;
    MapGridView map;

    private void turnOnBluetooth() {
        // TODO Auto-generated method stub
        Intent onBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(onBluetooth, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    private void runPlotMap2() {
        int startPlottingIndex = 0;
        for (int row = map.numRows; row >= 1; row--) {

            Log.d(TAG, "Row: " + row);

            for (int column = 1; column <= map.numColumns; column++) {
                String haveObstacles = binaryValues.substring(startPlottingIndex, (startPlottingIndex + 1));
                switch (haveObstacles) {
                    case "1":
                        Log.d(TAG, "Column: " + column);
                        map.createObstacle(row, column);
                        map.invalidate();
                        break;
                    case "0":
                        map.removeObstacle(row, column);
                        map.invalidate();
                        break;
                }
                startPlottingIndex = startPlottingIndex + 1;
            }
        }


        int a = Integer.parseInt(robotHeadX);
        int b = Integer.parseInt(robotHeadY);
        int c = Integer.parseInt(robotHeadFacing);
        if (firstTime) {
            map.robotBody[0][0] = true;
            map.robotBody[0][1] = true;
            map.robotBody[0][2] = true;

            map.robotBody[1][0] = true;
            map.robotCenter[1][1] = true;
            map.robotBody[1][2] = true;

            map.robotBody[2][0] = true;
            map.robotFront[2][1] = true;
            map.robotBody[2][2] = true;
            map.invalidate();
            firstTime = false;
        }
        map.updateRobotCoords(a, b, c);

    }

    private void runPlotMap() {
        int startPlottingIndex = 0;
        for (int row = map.numRows; row >= 1; row--) {

            Log.d(TAG, "Row: " + row);

            for (int column = 1; column <= map.numColumns; column++) {
                String haveObstacles = binaryValues.substring(startPlottingIndex, (startPlottingIndex + 1));
                switch (haveObstacles) {
                    case "1":
                        Log.d(TAG, "Column: " + column);
                        map.createObstacle(row, column);
                        map.invalidate();
                        break;
                    case "0":
                        map.removeObstacle(row, column);
                        map.invalidate();
                        break;
                }
                startPlottingIndex = startPlottingIndex + 1;
            }
        }
    }

    private void showToastMessage(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_main);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setTitle("Not Connected");


        map = (MapGridView) findViewById(R.id.mapGridView);
        map.addCollisionListener(this);

        robotStatus = (TextView) findViewById(R.id.robotStatus);
        robotCoordinate = (EditText) findViewById(R.id.string_robotCoordinate);
        robotWayPointCoordinate = (EditText) findViewById(R.id.string_robotWayPointCoordinate);
        setWayPoint = (Button) findViewById(R.id.button_setWayPoint);
        searchButton = (Button) findViewById(R.id.button_searchDevice);
        startShortest = (Button) findViewById(R.id.button_startShortest);
        startExplore = (Button) findViewById(R.id.button_startExplore);
        configureButton = (Button) findViewById(R.id.button_loadData);
        setRobotPosition = (Button) findViewById(R.id.button_setRobotPosition);
        deleteAllObstacle = (Button) findViewById(R.id.button_deleteAllObstacle);
        deleteWayPoint = (Button) findViewById(R.id.button_deleteWayPoint);
        deleteRobot = (Button) findViewById(R.id.button_deleteAllRobot);
        forwardButton = (ImageButton) findViewById(R.id.button_forward);
        reverseButton = (ImageButton) findViewById(R.id.button_reverse);
        updateMap = (Button) findViewById(R.id.button_updateMap);
        mapUpdateStatus = (Switch) findViewById(R.id.switch_mapUpdateStatus);
        uTurn = (ImageButton) findViewById(R.id.button_uTurn);

        /* Button not in used
        leftButton = (ImageButton) findViewById(R.id.button_left);
        rightButton = (ImageButton) findViewById(R.id.button_right);
        */

        rotateLeftButton = (ImageButton) findViewById(R.id.button_rotateLeft);
        rotateRightButton = (ImageButton) findViewById(R.id.button_rotateRight);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            showToastMessage("Bluetooth is not found");
            finish();
        }

        updateMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    runPlotMap();
                    //runPlotMap2();
                    showToastMessage("Map updated");
                } catch (Exception e) {
                    Log.d(TAG, "Error in retrieving map");
                }
            }
        });

        robotWayPointCoordinate.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String info1 = "Waypoint is placed in (" + robotWayPointCoordinate.getText().toString() + ")";
                showToastMessage(info1);
                Log.d(TAG, info1);
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                builder.setMessage("Set Way point?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        showToastMessage("Way Point set");
                        sendMessage("P" + robotWayPointCoordinate.getText().toString());
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        map.removeWayPoint();
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        robotCoordinate.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
                String info = "Top right of the robot is placed in (" + robotCoordinate.getText().toString() + ")";
                Log.d(TAG, info);
                showToastMessage(info);
                sendMessage(info);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mapUpdateStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // If the switch button is on
                    updateMap.setVisibility(View.INVISIBLE);
                } else {
                    // If the switch button is off
                    updateMap.setVisibility(View.VISIBLE);
                }
            }
        });

        setWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotHeadPosition = false;
                robotWayPoint = true;
                setWayPoint.setTextColor(Color.BLACK);
                setRobotPosition.setTextColor(Color.RED);
                showToastMessage("Set Waypoint");
            }
        });

        setRobotPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                robotHeadPosition = true;
                robotWayPoint = false;
                setRobotPosition.setTextColor(Color.BLACK);
                setWayPoint.setTextColor(Color.RED);
                showToastMessage("Place the Robot");

                /* Dialog Input
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                LayoutInflater inflater = screenMain.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.dialog_place_a_robot, null);

                builder.setView(dialogView);
                builder.setMessage("Set the coordinate (X,Y) of the Robot's head");
                builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //dialog
                        EditText CoordinateX = (EditText) dialogView.findViewById(R.id.robotCoordX);
                        EditText CoordinateY = (EditText) dialogView.findViewById(R.id.robotCoordY);

                        //sendMessage("Head of the robot's start coordinate (X,Y) is (" + x + "," + y + ")");

                        for (int col = 0; col < map.numColumns; col++) {
                            for (int row = 0; row < map.numRows; row++) {
                                map.robotFront[row][col] = false;
                                map.robotBody[row][col] = false;
                                map.robotCenter[row][col] = false;

                                map.invalidate();

                                try {

                                    x = Integer.parseInt(CoordinateX.getText().toString());
                                    y = Integer.parseInt(CoordinateY.getText().toString());

                                    map.robotBody[(y - 2)][(x - 1)] = true;
                                    map.robotBody[(y - 2)][x] = true;
                                    map.robotBody[(y - 2)][(x + 1)] = true;

                                    map.robotBody[(y - 1)][(x - 1)] = true;
                                    map.robotCenter[(y - 1)][x] = true;
                                    map.robotBody[(y - 1)][(x + 1)] = true;

                                    map.robotBody[y][(x - 1)] = true;
                                    map.robotFront[y][x] = true;
                                    map.robotBody[y][(x + 1)] = true;

                                    map.invalidate();
                                } catch (Exception e) {
                                    Log.d(TAG, "Coordinates not specified");
                                }
                            }
                        }
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                */
            }
        });

        startExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage("Exploration has started");
                sendMessage("Pexs");
            }
        });

        startShortest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToastMessage("Shortest path has started");
                if (!robotWayPointCoordinate.equals("")) {
                    //sendMessage("P" + robotWayPointCoordinate.getText().toString());
                    sendMessage("Pfps");
                    Log.d(TAG, "Sent Waypoint: " + robotWayPointCoordinate.getText().toString());
                }
            }
        });

        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("AM01");
                robotStatus.setText("Forward");
                map.moveForward();
            }
        });

        uTurn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map.currentAngle == 0) {
                    map.turnLeft();
                    map.turnLeft();
                } else if (map.currentAngle == 180) {
                    map.turnLeft();
                    map.turnLeft();
                }
                sendMessage("AU");
                robotStatus.setText("180 Rotate");
            }
        });

        forwardButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handler != null) return true;
                        handler = new Handler();
                        handler.postDelayed(mAction, 250);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (handler == null) return true;
                        handler.removeCallbacks(mAction);
                        handler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    map.moveForward();
                    handler.postDelayed(this, 1000);
                }
            };
        });

        reverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("As");
                robotStatus.setText("Reverse");
                map.moveBackward();
            }
        });

        reverseButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handler != null) return true;
                        handler = new Handler();
                        handler.postDelayed(mAction, 250);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (handler == null) return true;
                        handler.removeCallbacks(mAction);
                        handler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    map.moveBackward();
                    handler.postDelayed(this, 1000);
                }
            };
        });

        /* Action not in used
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("sl");
            }
        });

        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("sr");
            }
        });
        */

        rotateLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("AL");
                robotStatus.setText("Rotate Left");
                map.turnLeft();
            }
        });

        rotateLeftButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handler != null) return true;
                        handler = new Handler();
                        handler.postDelayed(mAction, 250);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (handler == null) return true;
                        handler.removeCallbacks(mAction);
                        handler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    map.turnLeft();
                    handler.postDelayed(this, 100);
                }
            };
        });

        rotateRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage("AR");
                robotStatus.setText("Rotate Right");
                map.turnRight();
            }
        });

        rotateRightButton.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (handler != null) return true;
                        handler = new Handler();
                        handler.postDelayed(mAction, 250);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (handler == null) return true;
                        handler.removeCallbacks(mAction);
                        handler = null;
                        break;
                }
                return false;
            }

            Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    map.turnLeft();
                    handler.postDelayed(this, 100);
                }
            };
        });

        deleteWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                builder.setMessage("Remove Waypoint?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        map.removeWayPoint();
                        showToastMessage("Waypoint removed");
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });


        deleteAllObstacle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                builder.setMessage("Remove ALL obstacles?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        map.removeExistingObstacles();
                        showToastMessage("All obstacles removed");
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });

        deleteRobot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                builder.setMessage("Remove robot?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        for (int col = 0; col < map.numColumns; col++) {
                            for (int row = 0; row < map.numRows; row++) {
                                map.robotFront[row][col] = false;
                                map.robotBody[row][col] = false;
                                map.robotCenter[row][col] = false;
                                map.currentAngle = 0;

                                map.invalidate();
                            }
                        }
                        showToastMessage("Robot is removed");
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }

        });


        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent serverIntent = new Intent(getApplicationContext(), screenSelectDeviceList.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        });

        configureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent persistentIntent = new Intent(getApplicationContext(), screenPersistentData.class);
                startActivityForResult(persistentIntent, REQUEST_PERSISTENT_DATA);
            }
        });

    }


    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handleMessage()");

            //super.handleMessage(msg);
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {

                        case BluetoothService.STATE_CONNECTED:
                            setTitle("Connected to '" + mConnectedDeviceName + "'");
                            robotStatus.setText("");
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            setTitle("Connecting...");
                            break;
                        /*
                        case BluetoothService.STATE_LISTEN:
                            break;
                        */
                        case BluetoothService.STATE_NONE:
                            setTitle("Not Connected");
                            break;
                    }
                    break;

                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.i(TAG, readMessage);
                    String testSubString = readMessage.substring(2,3);
                    Log.i(TAG, "Check substring: "+testSubString);

                    if (readMessage.length() > 8) {
                        if (readMessage.substring(2, 8).equalsIgnoreCase("status")) {
                            String robotStatusInformation = readMessage.substring(11, (readMessage.length() - 2));
                            robotStatus.setText(robotStatusInformation);
                        } else if (readMessage.substring(2, 6).equalsIgnoreCase("grid")) {
                            String hexD = readMessage.substring(11, (readMessage.length() - 2));

                            gridValues.clear();
                            binaryValues = "";

                            //Group them in 5 (FFFFF)
                            int indexOfMarker = 5;
                            int startIndex = 0;
                            for (int i = 0; i < 15; i++) {
                                /* Just the map */
                                String value = hexD.substring(startIndex, indexOfMarker);
                                gridValues.add(value);
                                startIndex = indexOfMarker;
                                indexOfMarker += 5;
                                Log.d(TAG, gridValues.get(i));

                                //Change them to binary and pad 0s if it is MSB is 0
                                String bin = new BigInteger(gridValues.get(i), 16).toString(2);
                                bin = String.format("%20s", bin).replace(" ", "0");
                                //Binary of the obstacles
                                binaryValues += bin;
                                /* Just the map */
                            }

                            if (mapUpdateStatus.isChecked()) {
                                runPlotMap();
                            }

                            Log.d(TAG, "Binary of the Grid: " + binaryValues);
                            Log.i(TAG, hexD);

                        } else if (readMessage.substring(2, 7).equalsIgnoreCase("robot")) {
                            //{"robot" : "000008000000000000000000000000000000000000000000000000000000000000000000000,13,4,0"}
                            String[] parts = readMessage.split("\"");
                            String[] info = parts[3].split(",", 2);
                            String hexD2 = info[0];
                            String hexRobotInfo = info[1];
//                            String hexD2 = readMessage.substring(12, 87);
//                            String hexRobotInfo = readMessage.substring(88, (readMessage.length() - 2));
                            Log.d(TAG, "HexD2: " + hexD2);

                            gridValues.clear();
                            binaryValues = "";

                            //Group them in 5 (FFFFF)
                            int indexOfMarker = 5;
                            int startIndex = 0;
                            for (int i = 0; i < 15; i++) {
                                /* Just the map */
                                String value = hexD2.substring(startIndex, indexOfMarker);
                                gridValues.add(value);
                                startIndex = indexOfMarker;
                                indexOfMarker += 5;
                                Log.d(TAG, gridValues.get(i));

                                //Change them to binary and pad 0s if it is MSB is 0
                                String bin = new BigInteger(gridValues.get(i), 16).toString(2);
                                bin = String.format("%20s", bin).replace(" ", "0");
                                //Binary of the obstacles
                                binaryValues += bin;
                                /* Just the map */
                            }

                            Log.d(TAG, "hexRobotInfo: " + hexRobotInfo);

                            String[] split = hexRobotInfo.split(",");

                            robotHeadX = split[0];
                            robotHeadY = split[1];
                            robotHeadFacing = split[2];

                            Log.d(TAG, "robotHeadX: " + robotHeadX);
                            Log.d(TAG, "robotHeadY: " + robotHeadY);
                            Log.d(TAG, "robotHeadFacing: " + robotHeadFacing);

                            if (mapUpdateStatus.isChecked()) {
                                runPlotMap2();
                            }


                        }else if(readMessage.substring(1,16).equalsIgnoreCase("finaldescriptor")){
                            Log.d(TAG,"In loop: "+readMessage);
                            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(screenMain.this, R.style.myDialog));
                            builder.setMessage(readMessage);
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }

                    } else {
                        showToastMessage("Thrash is received but not displayed");
                    }

                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != getApplicationContext()) {
                        showToastMessage("Connected to " + mConnectedDeviceName);
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != getApplicationContext()) {
                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "Bluetooth not enabled");
                    showToastMessage("An error has occurred in Bluetooth");
                }
                break;
            case REQUEST_PERSISTENT_DATA:
                // When the request to load persistent data returns
                if (resultCode == Activity.RESULT_OK) {
                    sendMessage(data.getExtras().getString(screenPersistentData.EXTRA_PERSISTENT_DATA));
                }
        }
    }


    //Set up the UI and background operations for chat.
    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothService.STATE_CONNECTED) {
            showToastMessage("You are not connected to a Bluetooth device");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    /**
     private void setStatus(int resId) {
     FragmentActivity activity = getActivity();
     if (null == activity) {
     return;
     }
     final ActionBar actionBar = activity.getActionBar();
     if (null == actionBar) {
     return;
     }
     actionBar.setSubtitle(resId);
     }
     **/

    /**
     * Establish connection with other device
     *
     * @param data   An {@link Intent} with {@link screenSelectDeviceList#EXTRA_DEVICE_ADDRESS} extra.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(screenSelectDeviceList.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCollide() {
        showToastMessage("Collision Detected");
    }

    @Override
    public void onHitWayPoint() {
        showToastMessage("Waypoint Found");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        map.removeCollisionListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()) {
            turnOnBluetooth();
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }
}