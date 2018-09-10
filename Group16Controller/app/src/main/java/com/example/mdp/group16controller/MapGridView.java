package com.example.mdp.group16controller;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;


public class MapGridView extends View {

    private static final String TAG = "onMotion";

    public interface Listener {
        void onCollide();

        void onHitWayPoint();
    }


    public int numColumns;
    public int numRows;
    private int cellWidth, cellHeight;
    private ArrayList<Listener> listeners = new ArrayList<Listener>();

    private Paint obstaclePaint = new Paint();
    private Paint mapPaint = new Paint();
    private Paint whiteFill = new Paint();
    private Paint wayPointFill = new Paint();
    private Paint robotBodyColor = new Paint();
    private Paint robotDirectionColor = new Paint();

    public static boolean[][] robotFront;
    public static boolean[][] robotCenter;
    public static boolean[][] robotBody;
    private boolean[][] cellChecked;
    private boolean arraysValid;
    public int currentAngle;


    private boolean[][] wayPointChecked;
    private boolean wayPointOnMap = false;
    private static int wayPointRow;
    private static int wayPointColumn;


    public MapGridView(Context context) {
        this(context, null);
    }

    public MapGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //create dimension size of the maze
        setNumColumns(15);
        setNumRows(20);

        //Robot Style
        robotBodyColor.setColor(ContextCompat.getColor(context, R.color.Orange));
        robotDirectionColor.setColor(ContextCompat.getColor(context, R.color.Blue));
    }

    public void addCollisionListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeCollisionListener(Listener listener) {
        listeners.remove(listener);
    }

    public void setNumColumns(int numColumns) {
        this.numColumns = numColumns;
        arraysValid = false;
        calculateDimensions(this.getWidth(), this.getHeight());
    }

    public int getNumColumns() {
        return numColumns;
    }

    public void setNumRows(int numRows) {
        this.numRows = numRows;
        arraysValid = false;
        calculateDimensions(this.getWidth(), this.getHeight());
    }

    public int getNumRows() {
        return numRows;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        calculateDimensions(this.getWidth(), this.getHeight());
    }

    private void calculateDimensions(int x, int y) {
        if (numColumns != 0 && numRows != 0) {
            cellWidth = getWidth() / numColumns;
            cellHeight = getHeight() / numRows;

            if (!arraysValid) {
                cellChecked = new boolean[numRows][numColumns];
                robotFront = new boolean[numRows][numColumns];
                robotBody = new boolean[numRows][numColumns];
                robotCenter = new boolean[numRows][numColumns];
                wayPointChecked = new boolean[numRows][numColumns];
                arraysValid = true;

                robotFront[2][1] = true;
                robotBody[2][0] = true;
                robotBody[2][2] = true;

                robotCenter[1][1] = true;
                robotBody[1][0] = true;
                robotBody[1][2] = true;

                robotBody[0][1] = true;
                robotBody[0][0] = true;
                robotBody[0][2] = true;
            }

            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();

        mapPaint.setColor(Color.BLACK);
        mapPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(0, 0, getWidth(), getHeight(), mapPaint);

        if (numColumns == 0 || numRows == 0)
            return;

        int width = getWidth();
        int height = getHeight();

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (robotCenter[i][j]) {
                    canvas.drawRect(j * cellWidth, (numRows - 1 - i) * (cellHeight), (j + 1) * cellWidth, (numRows - 1 - i + 1) * (cellHeight), robotBodyColor);
                    //cellChecked[i][j] = true;
                } else if (robotFront[i][j]) {
                    canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, robotDirectionColor);
                } else if (robotBody[i][j]) {
                    canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, robotBodyColor);
                } else {
                    if (!cellChecked[i][j] && !wayPointChecked[i][j]) {
                        whiteFill.setStyle(Paint.Style.FILL_AND_STROKE);
                        whiteFill.setColor(Color.WHITE);
                        canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, whiteFill);
                    } else if (cellChecked[i][j] && !wayPointChecked[i][j]) {
                        obstaclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        obstaclePaint.setColor(Color.BLACK);
                        canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, obstaclePaint);
                    } else if (cellChecked[i][j] && wayPointChecked[i][j]) {
                        wayPointFill.setStyle(Paint.Style.FILL_AND_STROKE);
                        wayPointFill.setColor(Color.MAGENTA);
                        canvas.drawRect(j * cellWidth, (numRows - 1 - i) * cellHeight, (j + 1) * cellWidth, (numRows - 1 - i + 1) * cellHeight, wayPointFill);
                    }
                }
            }

        }
        //drawing lines
        for (int i = 0; i < numColumns + 1; i++) {
            canvas.drawLine(i * cellWidth, 0, i * cellWidth, height, mapPaint);
        }

        for (int i = 0; i < numRows + 1; i++) {
            canvas.drawLine(0, i * cellHeight, width, i * cellHeight, mapPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            float xpoint = event.getX();
            float ypoint = event.getY();
            int cellCol, cellRow;

            cellCol = (int) Math.floor(xpoint / cellWidth);
            cellRow = (int) Math.floor((getHeight() - ypoint) / cellHeight);


            Log.d(TAG, "TRUE CELLCOL/CELLROW " + cellCol + " " + cellRow);

            if (screenMain.robotHeadPosition) {
                Log.d(TAG, String.valueOf(screenMain.robotHeadPosition));
                for (int j = 0; j < numColumns; j++) {
                    for (int i = 0; i < numRows; i++) {
                        robotFront[i][j] = false;
                        robotBody[i][j] = false;
                        robotCenter[i][j] = false;
                        currentAngle = 0;
                    }
                }
                invalidate();
                try {
                    robotBody[cellRow - 2][cellCol] = true;
                    robotBody[cellRow - 2][cellCol - 1] = true;
                    robotBody[cellRow - 2][cellCol - 2] = true;

                    robotBody[cellRow - 1][cellCol - 2] = true;
                    robotCenter[cellRow - 1][cellCol - 1] = true;
                    robotBody[cellRow - 1][cellCol] = true;

                    robotBody[cellRow][cellCol] = true;
                    robotFront[cellRow][cellCol - 1] = true;
                    robotBody[cellRow][cellCol - 2] = true;

                    screenMain.robotCoordinate.setText(cellRow + "," + cellCol);

                    invalidate();
                } catch (Exception e) {
                    Log.d(TAG, "Robot is out of grid");
                }
            } else if (screenMain.robotWayPoint) {
                if (!cellChecked[cellRow][cellCol]) {
                    Log.d(TAG, String.valueOf(screenMain.robotWayPoint));
                    if (wayPointOnMap == false) {
                        wayPointChecked[cellRow][cellCol] = true;
                        cellChecked[cellRow][cellCol] = true;
                        wayPointRow = cellRow;
                        wayPointOnMap = true;
                        wayPointColumn = cellCol;
                    } else if (wayPointOnMap == true) {
                        cellChecked[wayPointRow][wayPointColumn] = false;
                        wayPointChecked[wayPointRow][wayPointColumn] = false;
                        wayPointChecked[cellRow][cellCol] = true;
                        cellChecked[cellRow][cellCol] = true;
                        wayPointRow = cellRow;
                        wayPointColumn = cellCol;
                        wayPointOnMap = true;
                    }
                    invalidate();
                    screenMain.robotWayPointCoordinate.setText(cellCol + "," + cellRow);
                } else {
                    Log.d(TAG, "An obstacle is placed there");
                }
            } else {
                Log.d(TAG, "No option is selected");
            }
        }
        return true;
    }

    public void updateRobotCoords(int row, int col, int dir) {
        int columnFront = -1;
        int rowFront = -1;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (robotFront[i][j]) {
                    rowFront = i;
                    columnFront = j;
                }
            }
        }

        //remove robot's old center
        robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];

        //remove robot's center and body
        if (currentAngle == 0) {
            //robot facing north
            robotCenter[rowFront - 1][columnFront] = !robotCenter[rowFront - 1][columnFront];
            robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
            robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
            robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
            robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
            robotBody[rowFront - 2][columnFront - 1] = !robotBody[rowFront - 2][columnFront - 1];
            robotBody[rowFront - 2][columnFront] = !robotBody[rowFront - 2][columnFront];
            robotBody[rowFront - 2][columnFront + 1] = !robotBody[rowFront - 2][columnFront + 1];
        } else if (currentAngle == 90) {
            //robot facing east
            robotCenter[rowFront][columnFront - 1] = !robotCenter[rowFront][columnFront - 1];
            robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
            robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
            robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
            robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
            robotBody[rowFront - 1][columnFront - 2] = !robotBody[rowFront - 1][columnFront - 2];
            robotBody[rowFront][columnFront - 2] = !robotBody[rowFront][columnFront - 2];
            robotBody[rowFront + 1][columnFront - 2] = !robotBody[rowFront + 1][columnFront - 2];
        } else if (currentAngle == 180) {
            //robot facing south
            robotCenter[rowFront + 1][columnFront] = !robotCenter[rowFront + 1][columnFront];
            robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
            robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
            robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
            robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
            robotBody[rowFront + 2][columnFront - 1] = !robotBody[rowFront + 2][columnFront - 1];
            robotBody[rowFront + 2][columnFront] = !robotBody[rowFront + 2][columnFront];
            robotBody[rowFront + 2][columnFront + 1] = !robotBody[rowFront + 2][columnFront + 1];
        } else {
            //robot facing west
            robotCenter[rowFront][columnFront + 1] = !robotCenter[rowFront][columnFront + 1];
            robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
            robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
            robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
            robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
            robotBody[rowFront - 1][columnFront + 2] = !robotBody[rowFront - 1][columnFront + 2];
            robotBody[rowFront][columnFront + 2] = !robotBody[rowFront][columnFront + 2];
            robotBody[rowFront + 1][columnFront + 2] = !robotBody[rowFront + 1][columnFront + 2];
        }


        //update robot position with respect to position (N, S, E, W)
        if (dir == 0) {
            robotFront[row][col - 1] = !robotFront[row][col - 1];
            robotBody[row][col - 2] = !robotBody[row][col - 2];
            robotBody[row][col] = !robotBody[row][col];

            robotBody[row - 1][col - 2] = !robotBody[row - 1][col - 2];
            robotBody[row - 1][col] = !robotBody[row - 1][col];
            robotBody[row - 2][col - 1] = !robotBody[row - 2][col - 1];
            robotBody[row - 2][col - 2] = !robotBody[row - 2][col - 2];
            robotBody[row - 2][col] = !robotBody[row - 2][col];
            currentAngle = 0;
        } else if (dir == 180) {
            robotFront[row - 2][col - 1] = !robotFront[row - 2][col - 1];
            robotBody[row - 2][col - 2] = !robotBody[row - 2][col - 2];
            robotBody[row - 2][col] = !robotBody[row - 2][col];
            robotBody[row - 1][col - 2] = !robotBody[row - 1][col - 2];
            robotBody[row - 1][col] = !robotBody[row - 1][col];
            robotBody[row][col - 1] = !robotBody[row][col - 1];
            robotBody[row][col - 2] = !robotBody[row][col - 2];
            robotBody[row][col] = !robotBody[row][col];
            currentAngle = 180;
        } else if (dir == 270) {
            robotFront[row - 1][col - 2] = !robotFront[row - 1][col - 2];
            robotBody[row - 2][col - 2] = !robotBody[row - 2][col - 2];
            robotBody[row][col - 2] = !robotBody[row][col - 2];
            robotBody[row - 2][col - 1] = !robotBody[row - 2][col - 1];
            robotBody[row][col - 1] = !robotBody[row][col - 1];
            robotBody[row - 2][col] = !robotBody[row - 2][col];
            robotBody[row - 1][col] = !robotBody[row - 1][col];
            robotBody[row][col] = !robotBody[row][col];
            currentAngle = 270;
        } else {
            robotFront[row - 1][col] = !robotFront[row - 1][col];
            robotBody[row - 2][col] = !robotBody[row - 2][col];
            robotBody[row][col] = !robotBody[row][col];
            robotBody[row - 2][col - 1] = !robotBody[row - 2][col - 1];
            robotBody[row][col - 1] = !robotBody[row][col - 1];
            robotBody[row - 2][col - 2] = !robotBody[row - 2][col - 2];
            robotBody[row - 1][col - 2] = !robotBody[row - 1][col - 2];
            robotBody[row][col - 2] = !robotBody[row][col - 2];
            currentAngle = 90;
        }
        robotCenter[row - 1][col - 1] = !robotCenter[row - 1][col - 1];

        invalidate();
    }

    public void moveForward() {
        int columnFront = 0;
        int rowFront = 0;
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //facing N and not going outside arena
            if (rowFront + 1 != numRows) {
                //if the cell type moving towards is not a wall
                if (cellChecked[rowFront + 1][columnFront - 1] != true && cellChecked[rowFront + 1][columnFront] != true && cellChecked[rowFront + 1][columnFront + 1] != true) {
                    Log.d(TAG, "Waypoint: " + wayPointOnMap);
                    if (wayPointOnMap) {
                        if (cellChecked[rowFront + 2][columnFront - 1] == wayPointChecked[wayPointRow][wayPointColumn]
                                || cellChecked[rowFront + 2][columnFront] == wayPointChecked[wayPointRow][wayPointColumn]
                                || cellChecked[rowFront + 2][columnFront + 1] == wayPointChecked[wayPointRow][wayPointColumn]) {
                            Log.d(TAG, "2. rowFont value: " + (rowFront + 2));
                            Log.d(TAG, "2. wayPointRow value: " + wayPointRow);
                            wayPointChecked[wayPointRow][wayPointColumn] = false;
                            cellChecked[rowFront + 2][columnFront - 1] = false;
                            cellChecked[rowFront + 2][columnFront] = false;
                            cellChecked[rowFront + 2][columnFront + 1] = false;
                            wayPointOnMap = false;
                            invalidate();

                            for (Listener listener : listeners)
                                listener.onHitWayPoint();
                        }
                    }

                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotCenter[rowFront - 1][columnFront] = !robotCenter[rowFront - 1][columnFront];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotBody[rowFront - 2][columnFront + 1] = !robotBody[rowFront - 2][columnFront + 1];
                    robotBody[rowFront - 2][columnFront] = !robotBody[rowFront - 2][columnFront];
                    robotBody[rowFront - 2][columnFront - 1] = !robotBody[rowFront - 2][columnFront - 1];

                    //Paint the new robot
                    robotFront[rowFront + 1][columnFront] = !robotFront[rowFront + 1][columnFront];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotCenter[rowFront][columnFront] = !robotCenter[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }
        }
        //facing E
        else if (currentAngle == 90)

        {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (columnFront + 1 != numColumns) {
                if (cellChecked[rowFront + 1][columnFront + 1] != true && cellChecked[rowFront][columnFront + 1] != true && cellChecked[rowFront - 1][columnFront + 1] != true) {
                    Log.d(TAG, "Column value: " + (columnFront + 1));
                    Log.d(TAG, "wayPointColumn value: " + wayPointColumn);
                    if (wayPointOnMap) {
                        if ((cellChecked[rowFront + 1][columnFront + 2] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront][columnFront + 2] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront - 1][columnFront + 2] == wayPointChecked[wayPointRow][wayPointColumn]) && wayPointOnMap == true) {
                            Log.d(TAG, "2. Column value: " + (columnFront + 2));
                            Log.d(TAG, "2. wayPointColumn value: " + wayPointColumn);
                            wayPointChecked[wayPointRow][wayPointColumn] = false;
                            cellChecked[rowFront + 1][columnFront + 2] = false;
                            cellChecked[rowFront][columnFront + 2] = false;
                            cellChecked[rowFront - 1][columnFront + 2] = false;
                            wayPointOnMap = false;
                            invalidate();

                            for (Listener listener : listeners)
                                listener.onHitWayPoint();
                        }
                    }

                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotCenter[rowFront][columnFront - 1] = !robotCenter[rowFront][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 2] = !robotBody[rowFront + 1][columnFront - 2];
                    robotBody[rowFront][columnFront - 2] = !robotBody[rowFront][columnFront - 2];
                    robotBody[rowFront - 1][columnFront - 2] = !robotBody[rowFront - 1][columnFront - 2];

                    //Paint the new robot
                    robotFront[rowFront][columnFront + 1] = !robotFront[rowFront][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotCenter[rowFront][columnFront] = !robotCenter[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }

        }
        //facing S
        else if (currentAngle == 180)

        {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (rowFront - 1 != -1) {
                if (cellChecked[rowFront - 1][columnFront - 1] != true && cellChecked[rowFront - 1][columnFront] != true && cellChecked[rowFront - 1][columnFront + 1] != true) {

                    if (wayPointOnMap) {
                        if ((cellChecked[rowFront - 2][columnFront - 1] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront - 2][columnFront] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront - 2][columnFront + 1] == wayPointChecked[wayPointRow][wayPointColumn]) && wayPointOnMap == true) {
                            Log.d(TAG, "2. rowFont value: " + (rowFront + 2));
                            Log.d(TAG, "2. wayPointRow value: " + wayPointRow);
                            wayPointChecked[wayPointRow][wayPointColumn] = false;
                            cellChecked[rowFront - 2][columnFront - 1] = false;
                            cellChecked[rowFront - 2][columnFront] = false;
                            cellChecked[rowFront - 2][columnFront + 1] = false;
                            wayPointOnMap = false;
                            invalidate();

                            for (Listener listener : listeners)
                                listener.onHitWayPoint();
                        }
                    }

                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotCenter[rowFront + 1][columnFront] = !robotCenter[rowFront + 1][columnFront];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront + 2][columnFront + 1] = !robotBody[rowFront + 2][columnFront + 1];
                    robotBody[rowFront + 2][columnFront] = !robotBody[rowFront + 2][columnFront];
                    robotBody[rowFront + 2][columnFront - 1] = !robotBody[rowFront + 2][columnFront - 1];

                    //Paint the new robot
                    robotFront[rowFront - 1][columnFront] = !robotFront[rowFront - 1][columnFront];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotCenter[rowFront][columnFront] = !robotCenter[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }
        }
        //facing W
        else if (currentAngle == 270)

        {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (columnFront - 1 != -1) {
                if (cellChecked[rowFront - 1][columnFront - 1] != true && cellChecked[rowFront][columnFront - 1] != true && cellChecked[rowFront + 1][columnFront - 1] != true) {

                    Log.d(TAG, "Column value: " + (columnFront - 1));
                    Log.d(TAG, "wayPointColumn value: " + wayPointColumn);
                    if (wayPointOnMap) {
                        if ((cellChecked[rowFront - 1][columnFront - 2] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront][columnFront - 2] == wayPointChecked[wayPointRow][wayPointColumn] || cellChecked[rowFront + 1][columnFront - 2] == wayPointChecked[wayPointRow][wayPointColumn]) && wayPointOnMap == true) {
                            Log.d(TAG, "2. Column value: " + (columnFront - 2));
                            Log.d(TAG, "2. wayPointColumn value: " + wayPointColumn);
                            wayPointChecked[wayPointRow][wayPointColumn] = false;
                            cellChecked[rowFront + 1][columnFront - 2] = false;
                            cellChecked[rowFront][columnFront - 2] = false;
                            cellChecked[rowFront - 1][columnFront - 2] = false;
                            wayPointOnMap = false;
                            invalidate();

                            for (Listener listener : listeners)
                                listener.onHitWayPoint();
                        }
                    }

                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotCenter[rowFront][columnFront + 1] = !robotCenter[rowFront][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 2] = !robotBody[rowFront + 1][columnFront + 2];
                    robotBody[rowFront][columnFront + 2] = !robotBody[rowFront][columnFront + 2];
                    robotBody[rowFront - 1][columnFront + 2] = !robotBody[rowFront - 1][columnFront + 2];

                    //Paint the new robot
                    robotFront[rowFront][columnFront - 1] = !robotFront[rowFront][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotCenter[rowFront][columnFront] = !robotCenter[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }
        }

        invalidate();

    }

    public void turnLeft() {
        int columnFront = -1;
        int rowFront = -1;

        //facing N
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }

            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront - 1][columnFront - 1] = !robotFront[rowFront - 1][columnFront - 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from N to W
            currentAngle = 270;
        }
        //facing W
        else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront - 1][columnFront + 1] = !robotFront[rowFront - 1][columnFront + 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from W to S
            currentAngle = 180;

        }
        //facing S
        else if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront + 1][columnFront + 1] = !robotFront[rowFront + 1][columnFront + 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from S to E
            currentAngle = 90;

        }
        //facing E
        else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront + 1][columnFront - 1] = !robotFront[rowFront + 1][columnFront - 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from E to N
            currentAngle = 0;

        }
        invalidate();
    }

    public void turnRight() {
        int columnFront = -1;
        int rowFront = -1;
        //facing N
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        //cellChecked[i][j] = true;
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront - 1][columnFront + 1] = !robotFront[rowFront - 1][columnFront + 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from N to E
            currentAngle = 90;

        }
        //facing E
        else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront - 1][columnFront - 1] = !robotFront[rowFront - 1][columnFront - 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from E to S
            currentAngle = 180;

        }
        //facing S
        else if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront + 1][columnFront - 1] = !robotFront[rowFront + 1][columnFront - 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from S to W
            currentAngle = 270;

        }
        //facing W
        else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //Remove old robot (affected cells only)
            robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
            robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];

            //Paint new robot (affected cells only)
            robotFront[rowFront + 1][columnFront + 1] = !robotFront[rowFront + 1][columnFront + 1];
            robotBody[rowFront][columnFront] = !robotBody[rowFront][columnFront];

            //Update direction from W to N
            currentAngle = 0;
        }
        invalidate();
    }

    public void moveBackward() {
        int columnFront = -1;
        int rowFront = -1;
        if (currentAngle == 0) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            //facing N and not going outside arena
            if (rowFront - 3 >= 0) {
                //if the cell type reversing towards is not a wall
                if (cellChecked[rowFront - 3][columnFront - 1] != true && cellChecked[rowFront - 3][columnFront] != true && cellChecked[rowFront - 3][columnFront + 1] != true) {
                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotCenter[rowFront - 1][columnFront] = !robotCenter[rowFront - 1][columnFront];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotBody[rowFront - 2][columnFront + 1] = !robotBody[rowFront - 2][columnFront + 1];
                    robotBody[rowFront - 2][columnFront] = !robotBody[rowFront - 2][columnFront];
                    robotBody[rowFront - 2][columnFront - 1] = !robotBody[rowFront - 2][columnFront - 1];

                    //Paint the new robot
                    robotFront[rowFront - 1][columnFront] = !robotFront[rowFront - 1][columnFront];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotCenter[rowFront - 2][columnFront] = !robotCenter[rowFront - 2][columnFront];
                    robotBody[rowFront - 2][columnFront + 1] = !robotBody[rowFront - 2][columnFront + 1];
                    robotBody[rowFront - 2][columnFront - 1] = !robotBody[rowFront - 2][columnFront - 1];
                    robotBody[rowFront - 3][columnFront + 1] = !robotBody[rowFront - 3][columnFront + 1];
                    robotBody[rowFront - 3][columnFront] = !robotBody[rowFront - 3][columnFront];
                    robotBody[rowFront - 3][columnFront - 1] = !robotBody[rowFront - 3][columnFront - 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }
        }
        //facing E
        else if (currentAngle == 90) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (columnFront - 3 >= 0) {
                if (cellChecked[rowFront + 1][columnFront - 3] != true && cellChecked[rowFront][columnFront - 3] != true && cellChecked[rowFront - 1][columnFront - 3] != true) {
                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotCenter[rowFront][columnFront - 1] = !robotCenter[rowFront][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 2] = !robotBody[rowFront + 1][columnFront - 2];
                    robotBody[rowFront][columnFront - 2] = !robotBody[rowFront][columnFront - 2];
                    robotBody[rowFront - 1][columnFront - 2] = !robotBody[rowFront - 1][columnFront - 2];

                    //Paint the new robot
                    robotFront[rowFront][columnFront - 1] = !robotFront[rowFront][columnFront - 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront - 1][columnFront - 1] = !robotBody[rowFront - 1][columnFront - 1];
                    robotCenter[rowFront][columnFront - 2] = !robotCenter[rowFront][columnFront - 2];
                    robotBody[rowFront + 1][columnFront - 2] = !robotBody[rowFront + 1][columnFront - 2];
                    robotBody[rowFront - 1][columnFront - 2] = !robotBody[rowFront - 1][columnFront - 2];
                    robotBody[rowFront + 1][columnFront - 3] = !robotBody[rowFront + 1][columnFront - 3];
                    robotBody[rowFront][columnFront - 3] = !robotBody[rowFront][columnFront - 3];
                    robotBody[rowFront - 1][columnFront - 3] = !robotBody[rowFront - 1][columnFront - 3];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }

        }
        //facing S
        else if (currentAngle == 180) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (rowFront + 3 != numRows) {
                if (cellChecked[rowFront + 3][columnFront - 1] != true && cellChecked[rowFront + 3][columnFront] != true && cellChecked[rowFront + 3][columnFront + 1] != true) {
                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront][columnFront + 1] = !robotBody[rowFront][columnFront + 1];
                    robotBody[rowFront][columnFront - 1] = !robotBody[rowFront][columnFront - 1];
                    robotCenter[rowFront + 1][columnFront] = !robotCenter[rowFront + 1][columnFront];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotBody[rowFront + 2][columnFront + 1] = !robotBody[rowFront + 2][columnFront + 1];
                    robotBody[rowFront + 2][columnFront] = !robotBody[rowFront + 2][columnFront];
                    robotBody[rowFront + 2][columnFront - 1] = !robotBody[rowFront + 2][columnFront - 1];

                    //Paint the new robot
                    robotFront[rowFront + 1][columnFront] = !robotFront[rowFront + 1][columnFront];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront - 1] = !robotBody[rowFront + 1][columnFront - 1];
                    robotCenter[rowFront + 2][columnFront] = !robotCenter[rowFront + 2][columnFront];
                    robotBody[rowFront + 2][columnFront + 1] = !robotBody[rowFront + 2][columnFront + 1];
                    robotBody[rowFront + 2][columnFront - 1] = !robotBody[rowFront + 2][columnFront - 1];
                    robotBody[rowFront + 3][columnFront + 1] = !robotBody[rowFront + 3][columnFront + 1];
                    robotBody[rowFront + 3][columnFront] = !robotBody[rowFront + 3][columnFront];
                    robotBody[rowFront + 3][columnFront - 1] = !robotBody[rowFront + 3][columnFront - 1];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }

        }
        //facing W
        else if (currentAngle == 270) {
            for (int i = 0; i < numRows; i++) {
                for (int j = 0; j < numColumns; j++) {
                    if (robotFront[i][j]) {
                        rowFront = i;
                        columnFront = j;
                    }
                }
            }
            if (columnFront + 3 != numColumns) {
                if (cellChecked[rowFront - 1][columnFront + 3] != true && cellChecked[rowFront][columnFront + 3] != true && cellChecked[rowFront + 1][columnFront + 3] != true) {
                    //Clear the old robot
                    robotFront[rowFront][columnFront] = !robotFront[rowFront][columnFront];
                    robotBody[rowFront + 1][columnFront] = !robotBody[rowFront + 1][columnFront];
                    robotBody[rowFront - 1][columnFront] = !robotBody[rowFront - 1][columnFront];
                    robotCenter[rowFront][columnFront + 1] = !robotCenter[rowFront][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 2] = !robotBody[rowFront + 1][columnFront + 2];
                    robotBody[rowFront][columnFront + 2] = !robotBody[rowFront][columnFront + 2];
                    robotBody[rowFront - 1][columnFront + 2] = !robotBody[rowFront - 1][columnFront + 2];

                    //Paint the new robot
                    robotFront[rowFront][columnFront + 1] = !robotFront[rowFront][columnFront + 1];
                    robotBody[rowFront + 1][columnFront + 1] = !robotBody[rowFront + 1][columnFront + 1];
                    robotBody[rowFront - 1][columnFront + 1] = !robotBody[rowFront - 1][columnFront + 1];
                    robotCenter[rowFront][columnFront + 2] = !robotCenter[rowFront][columnFront + 2];
                    robotBody[rowFront + 1][columnFront + 2] = !robotBody[rowFront + 1][columnFront + 2];
                    robotBody[rowFront - 1][columnFront + 2] = !robotBody[rowFront - 1][columnFront + 2];
                    robotBody[rowFront + 1][columnFront + 3] = !robotBody[rowFront + 1][columnFront + 3];
                    robotBody[rowFront][columnFront + 3] = !robotBody[rowFront][columnFront + 3];
                    robotBody[rowFront - 1][columnFront + 3] = !robotBody[rowFront - 1][columnFront + 3];
                } else {
                    for (Listener listener : listeners)
                        listener.onCollide();
                }
            }
        }

        invalidate();
    }

    public void createObstacle(int row, int col) {
        cellChecked[row - 1][col - 1] = true;
        invalidate();
    }

    public void removeObstacle(int row, int col) {
        cellChecked[row - 1][col - 1] = false;
        invalidate();
    }

    public void removeExistingObstacles() {
        for (int col = 0; col < numColumns; col++) {
            for (int row = 0; row < numRows; row++) {
                if (!wayPointChecked[row][col])
                    cellChecked[row][col] = false;
            }
        }
        invalidate();
    }

    public void removeWayPoint() {
        wayPointChecked[wayPointRow][wayPointColumn] = false;
        cellChecked[wayPointRow][wayPointColumn] = false;
        wayPointOnMap = false;
        invalidate();
    }
}
