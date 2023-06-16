package com.origin.fountain.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.InputFilter;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import com.origin.fountain.DeviceData;
import com.origin.fountain.R;
import com.origin.fountain.Utils;
import com.origin.fountain.bluetooth.DeviceConnector;
import com.origin.fountain.bluetooth.DeviceListActivity;

public final class DeviceControlActivity extends BaseActivity {
    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String LOG = "LOG";

    // Подсветка crc
    private static final String CRC_OK = "#FFFF00";


    private static final String CRC_BAD = "#FF0000";

    private static final SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm:ss.SSS");

    private static String MSG_NOT_CONNECTED;
    private static String MSG_CONNECTING;
    private static String MSG_CONNECTED;

    private static DeviceConnector connector;
    private static BluetoothResponseHandler mHandler;

    private StringBuilder logHtml;
    private static TextView logTextView;
    private EditText commandEditText;

    // Настройки приложения
    private int logLimitSize;
    private boolean hexMode, checkSum, needClean, logLimit;
    private boolean show_timings, show_direction;
    private String command_ending;
    private String deviceName;

    private static String TotalCommand = "";

    private Button PlaySeq, StopSeq, PlayAll, RandomPlay, CreatSeq, DeleteSeq;
    static ListView ListSeq;
    private static List<Sequence> sequences = new ArrayList<Sequence>();
    static SeqAdapter adapter;
    private int seleted_seqId = 100;
    private String selected_seqence=null,selected_seqSpeed=null;
    public static final byte PLAY_SEQ     = 0x01;
    public static final byte STOP_SEQ      = 0x02;
    public static final byte PLAY_ALL = 0x03;
    public static final byte RANDOM_PLAY   = 0x04;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.settings_activity, false);

        if (mHandler == null) mHandler = new BluetoothResponseHandler(this);
        else mHandler.setTarget(this);

        MSG_NOT_CONNECTED = getString(R.string.msg_not_connected);
        MSG_CONNECTING = getString(R.string.msg_connecting);
        MSG_CONNECTED = getString(R.string.msg_connected);

        setContentView(R.layout.activity_terminal);
        if (isConnected() && (savedInstanceState != null)) {
            setDeviceName(savedInstanceState.getString(DEVICE_NAME));
        } else getActionBar().setSubtitle(MSG_NOT_CONNECTED);

        PlaySeq = (Button)findViewById(R.id.btn_play);
        StopSeq = (Button)findViewById(R.id.btn_stop);
        StopSeq.setClickable(false);
        PlayAll = (Button)findViewById(R.id.btn_playall);
        RandomPlay = (Button)findViewById(R.id.btn_playrandome);
        CreatSeq = (Button)findViewById(R.id.btn_create);
        DeleteSeq = (Button)findViewById(R.id.btn_delete);
        ListSeq = (ListView)findViewById(R.id.seqlist);
        InitButtonState();
//        initList();

        PlaySeq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter!=null){
                    InitButtonState();
                    PlaySeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));

                    selected_seqence = adapter.getSeqNamee();
                    selected_seqSpeed = adapter.getSeqSpeedd();
                    seleted_seqId = adapter.getSeqId();
                    if(seleted_seqId==1){
                        playSeq(selected_seqence,selected_seqSpeed);

                    }else{
                        Toast.makeText(DeviceControlActivity.this, "Please Select one of Sequence", Toast.LENGTH_SHORT).show();
                        PlaySeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
                    }
                }


            }
        });
        StopSeq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitButtonState();
                StopSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
                if(isConnected()){
                    stopSeq();
                }else{
                    Toast.makeText(DeviceControlActivity.this, "Not Device Connect!", Toast.LENGTH_SHORT).show();
                    StopSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
                }

            }
        });
        PlayAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitButtonState();
                PlayAll.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
                if(isConnected()){
                    playAll();
                }else{
                    Toast.makeText(DeviceControlActivity.this, "Not Device Connect!", Toast.LENGTH_SHORT).show();
                    PlayAll.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
                }

            }
        });
        RandomPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitButtonState();
                RandomPlay.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
                if(isConnected()){
                    playRandom();
                }else{
                    Toast.makeText(DeviceControlActivity.this, "Not Device Connect!", Toast.LENGTH_SHORT).show();
                    RandomPlay.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
                }

            }
        });
        CreatSeq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                addSeq();
                InitButtonState();
                CreatSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
                Intent myIntent = new Intent(DeviceControlActivity.this, WebviewActivity.class);
                DeviceControlActivity.this.startActivity(myIntent);

            }
        });
        DeleteSeq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitButtonState();
                DeleteSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
                if(seleted_seqId==0){
                    Toast.makeText(DeviceControlActivity.this, "Please Select one of Sequence", Toast.LENGTH_SHORT).show();
                    DeleteSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));

                }else{
                    //removeSeq(seleted_seqId);
                }


            }
        });



        this.logHtml = new StringBuilder();
        if (savedInstanceState != null) this.logHtml.append(savedInstanceState.getString(LOG));

//        this.logTextView = (TextView) findViewById(R.id.log_textview);
//        this.logTextView.setMovementMethod(new ScrollingMovementMethod());
//        this.logTextView.setText(Html.fromHtml(logHtml.toString()));

        this.commandEditText = (EditText) findViewById(R.id.command_edittext);
        // soft-keyboard send button
        this.commandEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    sendCommand(null);
                    return true;
                }
                return false;
            }
        });
        // hardware Enter button
        this.commandEditText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            sendCommand(null);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (sharedText != null) {
                    EditText commandEditText = (EditText)findViewById(R.id.command_edittext);
                    commandEditText.setText(sharedText);
                }
            }
        }
    }

    private void initList() {
        Sequence seq1 = new Sequence(1, "Seqeunce1","100");
        Sequence seq2 = new Sequence(2, "Seqeunce2","100");
        Sequence seq3 = new Sequence(3, "Seqeunce3","100");

        sequences.add(seq1);
        sequences.add(seq2);
        sequences.add(seq3);
        adapter = new SeqAdapter(this, sequences);
        ListSeq.setItemsCanFocus(true);

        ListSeq.setAdapter(adapter);
    }

    // ==========================================================================
    private void InitButtonState(){
        PlaySeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        StopSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        RandomPlay.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        PlayAll.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        CreatSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        DeleteSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
    }
    private void ButtonDisable(){
        PlaySeq.setClickable(false);
        StopSeq.setClickable(true);
        RandomPlay.setClickable(false);
        PlayAll.setClickable(false);
        CreatSeq.setClickable(false);
        DeleteSeq.setClickable(false);
        PlaySeq.setBackgroundResource(0);
        StopSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.button_border));
        RandomPlay.setBackgroundResource(0);
        PlayAll.setBackgroundResource(0);
        CreatSeq.setBackgroundResource(0);
        DeleteSeq.setBackgroundResource(0);
    }
    private void Delay(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Do something after 3s = 3000ms
               return;
            }
        }, 3000);
    }
    private void sendString(String seqString){

            if (seqString.isEmpty()) return;

            // Дополнение команд в hex
            if (hexMode && (seqString.length() % 2 == 1)) {
                seqString = "0" + seqString;
//                commandEditText.setText(seqString);
            }

            // checksum
            if (checkSum) {
                seqString += Utils.calcModulo256(seqString);
            }

            byte[] command = (hexMode ? Utils.toHex(seqString) : seqString.getBytes());
            if (command_ending != null) command = Utils.concat(command, command_ending.getBytes());
            if (isConnected()) {
                connector.write(command);
//                appendLog(commandString, hexMode, true, needClean);
            }

    }
    private void playSeq(String seq_name, String seq_speed) {
//        Toast.makeText(DeviceControlActivity.this, "item clicked "+selected_seqence, Toast.LENGTH_SHORT).show();

        String seqs = "playseq"+" " + seq_name + "," +seq_speed;
        sendString(seqs);

        ButtonDisable();
    }
    private void stopSeq(){
        String stopseqs = "stopseq";
        sendString(stopseqs);
        PlaySeq.setClickable(true);
        StopSeq.setClickable(false);
        RandomPlay.setClickable(true);
        PlayAll.setClickable(true);
        CreatSeq.setClickable(true);
        DeleteSeq.setClickable(true);
        InitButtonState();
        StopSeq.setBackground(ContextCompat.getDrawable(DeviceControlActivity.this, R.drawable.green_border));
    }
    private void playAll(){
        String allseqs = "playallseq";
        sendString(allseqs);
        ButtonDisable();
    }
    private void playRandom(){
        String randomseqs = "randomseq";
        sendString(randomseqs);
        ButtonDisable();
    }
    private void removeSeq(int seqId) {
//        sequences.remove(seqId-1);
//        // next thing you have to do is check if your adapter has changed
//        adapter.notifyDataSetChanged();
    }

    // ==========================================================================
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(DEVICE_NAME, deviceName);
//        if (logTextView != null) {
//            outState.putString(LOG, logHtml.toString());
//        }
    }
    // ============================================================================


    /**
     * Проверка готовности соединения
     */
    private boolean isConnected() {
        return (connector != null) && (connector.getState() == DeviceConnector.STATE_CONNECTED);
    }
    // ==========================================================================


    /**
     * Разорвать соединение
     */
    private void stopConnection() {
        if (connector != null) {
            connector.stop();
            connector = null;
            deviceName = null;
        }
    }
    // ==========================================================================


    /**
     * Список устройств для подключения
     */
    private void startDeviceListActivity() {
        stopConnection();
        Intent serverIntent = new Intent(this, DeviceListActivity.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }
    // ============================================================================


    /**
     * Обработка аппаратной кнопки "Поиск"
     *
     * @return
     */
    @Override
    public boolean onSearchRequested() {
        if (super.isAdapterReady()) startDeviceListActivity();
        return false;
    }
    // ==========================================================================


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_control_activity, menu);
        final MenuItem bluetooth = menu.findItem(R.id.menu_search);
        if (bluetooth != null) bluetooth.setIcon(this.isConnected() ?
                R.drawable.ic_action_device_bluetooth_connected :
                R.drawable.ic_action_device_bluetooth);
        return true;
    }
    // ============================================================================


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.menu_search:
                if (super.isAdapterReady()) {
                    if (isConnected()) stopConnection();
                    else startDeviceListActivity();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (checkBluetoothPermission(REQUEST_ENABLE_BT)) startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                return true;

            case R.id.menu_clear:
//                if (logTextView != null) logTextView.setText("");
                return true;

            case R.id.menu_send:
//                if (logTextView != null) {
//                    final String msg = logTextView.getText().toString();
//                    final Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setType("text/plain");
//                    intent.putExtra(Intent.EXTRA_TEXT, msg);
//                    startActivity(Intent.createChooser(intent, getString(R.string.menu_send)));
//                }
                return true;

            case R.id.menu_settings:
                final Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // ============================================================================


    @Override
    public void onStart() {
        super.onStart();

        // hex mode
        final String mode = Utils.getPrefence(this, getString(R.string.pref_commands_mode));
        this.hexMode = "HEX".equals(mode);
        if (hexMode) {
            commandEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            commandEditText.setFilters(new InputFilter[]{new Utils.InputFilterHex()});
        } else {
            commandEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            commandEditText.setFilters(new InputFilter[]{});
        }

        // checksum
        final String checkSum = Utils.getPrefence(this, getString(R.string.pref_checksum_mode));
        this.checkSum = "Modulo 256".equals(checkSum);

        // Окончание строки
        this.command_ending = getCommandEnding();

        // Формат отображения лога команд
        this.show_timings = Utils.getBooleanPrefence(this, getString(R.string.pref_log_timing));
        this.show_direction = Utils.getBooleanPrefence(this, getString(R.string.pref_log_direction));
        this.needClean = Utils.getBooleanPrefence(this, getString(R.string.pref_need_clean));
        this.logLimit = Utils.getBooleanPrefence(this, getString(R.string.pref_log_limit));
        this.logLimitSize = Utils.formatNumber(Utils.getPrefence(this, getString(R.string.pref_log_limit_size)));
    }
    // ============================================================================


    /**
     * Получить из настроек признак окончания команды
     */
    private String getCommandEnding() {
        String result = Utils.getPrefence(this, getString(R.string.pref_commands_ending));
        if (result.equals("\\r\\n")) result = "\r\n";
        else if (result.equals("\\n")) result = "\n";
        else if (result.equals("\\r")) result = "\r";
        else result = "";
        return result;
    }
    // ============================================================================


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = btAdapter.getRemoteDevice(address);
                    if (super.isAdapterReady() && (connector == null)) setupConnector(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                super.pendingRequestEnableBt = false;
                if (resultCode != Activity.RESULT_OK) {
                    Utils.log("BT not enabled");
                }
                break;
        }
    }
    // ==========================================================================


    /**
     * Установка соединения с устройством
     */
    private void setupConnector(BluetoothDevice connectedDevice) {
        stopConnection();
        try {
            String emptyName = getString(R.string.empty_device_name);
            DeviceData data = new DeviceData(connectedDevice, emptyName);
            connector = new DeviceConnector(data, mHandler);
            connector.connect();
        } catch (IllegalArgumentException e) {
            Utils.log("setupConnector failed: " + e.getMessage());
        }
    }
    // ==========================================================================


    /**
     * Отправка команды устройству
     */
    public void sendCommand(View view) {
        if (commandEditText != null) {
            String commandString = commandEditText.getText().toString();
            if (commandString.isEmpty()) return;

            // Дополнение команд в hex
            if (hexMode && (commandString.length() % 2 == 1)) {
                commandString = "0" + commandString;
                commandEditText.setText(commandString);
            }

            // checksum
            if (checkSum) {
                commandString += Utils.calcModulo256(commandString);
            }

            byte[] command = (hexMode ? Utils.toHex(commandString) : commandString.getBytes());
            if (command_ending != null) command = Utils.concat(command, command_ending.getBytes());
            if (isConnected()) {
                connector.write(command);
//                appendLog(commandString, hexMode, true, needClean);
            }
        }
    }
    // ==========================================================================



//    void appendLog(String message, boolean hexMode, boolean outgoing, boolean clean) {
//
//        // если установлено ограничение на логи, проверить и почистить
//        if (this.logLimit && this.logLimitSize > 0 && logTextView.getLineCount() > this.logLimitSize) {
//            logTextView.setText("");
//        }
//
//        StringBuilder msg = new StringBuilder();
//        if (show_timings) msg.append("[").append(timeformat.format(new Date())).append("]");
//        if (show_direction) {
//            final String arrow = (outgoing ? " << " : " >> ");
//            msg.append(arrow);
//        } else msg.append(" ");
//
//        // Убрать символы переноса строки \r\n
//        message = message.replace("\r", "").replace("\n", "");
//
//        // Проверка контрольной суммы ответа
//        String crc = "";
//        boolean crcOk = false;
//        if (checkSum) {
//            int crcPos = message.length() - 2;
//            crc = message.substring(crcPos);
//            message = message.substring(0, crcPos);
//            crcOk = outgoing || crc.equals(Utils.calcModulo256(message).toUpperCase());
//            if (hexMode) crc = Utils.printHex(crc.toUpperCase());
//        }
//
//        // Лог в html
//        msg.append("<b>")
//                .append(hexMode ? Utils.printHex(message) : message)
//                .append(checkSum ? Utils.mark(crc, crcOk ? CRC_OK : CRC_BAD) : "")
//                .append("</b>")
//                .append("<br>");
//
//        logHtml.append(msg);
//        logTextView.append(Html.fromHtml(msg.toString()));
//
//        final int scrollAmount = logTextView.getLayout().getLineTop(logTextView.getLineCount()) - logTextView.getHeight();
//        if (scrollAmount > 0)
//            logTextView.scrollTo(0, scrollAmount);
//        else logTextView.scrollTo(0, 0);
//
//        if (clean) commandEditText.setText("");
//    }
    // =========================================================================


    void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
        getActionBar().setSubtitle(deviceName);
    }


    // ==========================================================================

    /**
     * Обработчик приёма данных от bluetooth-потока
     */
    private static class BluetoothResponseHandler extends Handler {
        private WeakReference<DeviceControlActivity> mActivity;

        public BluetoothResponseHandler(DeviceControlActivity activity) {
            mActivity = new WeakReference<DeviceControlActivity>(activity);
        }

        public void setTarget(DeviceControlActivity target) {
            mActivity.clear();
            mActivity = new WeakReference<DeviceControlActivity>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            DeviceControlActivity activity = mActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case MESSAGE_STATE_CHANGE:

                        Utils.log("MESSAGE_STATE_CHANGE: " + msg.arg1);
                        final ActionBar bar = activity.getActionBar();
                        switch (msg.arg1) {
                            case DeviceConnector.STATE_CONNECTED:
                                bar.setSubtitle(MSG_CONNECTED);
                                sequences.clear();
                                break;
                            case DeviceConnector.STATE_CONNECTING:
                                bar.setSubtitle(MSG_CONNECTING);
                                break;
                            case DeviceConnector.STATE_NONE:
                                bar.setSubtitle(MSG_NOT_CONNECTED);
                                break;
                        }
                        activity.invalidateOptionsMenu();
                        break;

                    case MESSAGE_READ:
                        final String readMessage = (String) msg.obj;
                        if (readMessage != null) {
//                            activity.appendLog(readMessage, false, false, false);
//                            logTextView.setText(readMessage + "\n");
                            TotalCommand  = TotalCommand + readMessage;
//                            Sequence seq1 = new Sequence(1, readMessage,"100");
//                            sequences.add(seq1);
                        }
                        break;

                    case MESSAGE_DEVICE_NAME:
                        activity.setDeviceName((String) msg.obj);
                        break;

                    case MESSAGE_WRITE:
                        // stub
                        break;

                    case MESSAGE_TOAST:
                        // stub
                        break;
                }
            }
            String lines[] = TotalCommand.split("\\r?\\n");
            for(int i =0;i<lines.length;i++){
                if(!lines[i].isEmpty()){
                    Sequence seq1 = new Sequence(1, lines[i],"100");
                    sequences.add(seq1);
                }
            }
            TotalCommand = "";
            adapter = new SeqAdapter(activity, sequences);
            ListSeq.setItemsCanFocus(true);
            ListSeq.setAdapter(adapter);
        }

    }
    // ==========================================================================
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Fountain").setMessage("Are you sure you want to close Fountin?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sequences.clear();
                        finish();
//                        Toast.makeText(DeviceControlActivity.this, "Activity closed",Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("No", null).show();
    }
}