package com.example.myapplication2;

import android.app.Activity;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.myapplication2.databinding.ActivityMainBinding;
import com.example.myapplication2.databinding.ItemBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MainActivity extends AppCompatActivity {

    Logger log = Logger.getLogger(MainActivity.class.getName());

    public static int DEFAULT_TEMPO = 150;
    public static int DEFAULT_BEATS = 4;
    public static int DEFAULT_REPEAT = 4;
    public static int MINUTE = 60000;


    private AudioManager audio;
    private MetronomeAsyncTask metroTask;
    public Activity activity;

    public ActivityMainBinding mainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        mainBinding = DataBindingUtil.setContentView(activity, R.layout.activity_main);


        metroTask = new MetronomeAsyncTask();
        audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        layoutInflater =
                (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        configContainer = (LinearLayout) findViewById(R.id.configLayout);

        addConfig(0);

        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button b = (Button) v;
                if (metroTask != null && metroTask.getMetronome().getPlayBtnLabel() == "start" ) {
                    stopTimer();
                } else {
                    resetTimer();
                    metroTask.getMetronome().setPlayBtnLabel("stop");
                }
            }
        });
    }

    private void addConfig(int index) {
        final View config = layoutInflater.inflate(R.layout.config, null);

        configContainer.addView(config, index);

        NumberPicker tempoView = ((NumberPicker) config.findViewById(R.id.tempoPicker));
        tempoView.setMinValue(0);
        tempoView.setMaxValue(300);
        tempoView.setWrapSelectorWheel(false);
        tempoView.setValue(DEFAULT_TEMPO);

        NumberPicker beatsView = ((NumberPicker) config.findViewById(R.id.beatsPicker));
        beatsView.setMinValue(0);
        beatsView.setMaxValue(300);
        beatsView.setWrapSelectorWheel(false);
        beatsView.setValue(DEFAULT_BEATS);

        NumberPicker repeatView = ((NumberPicker) config.findViewById(R.id.repeatsPicker));
        repeatView.setMinValue(0);
        repeatView.setMaxValue(300);
        repeatView.setWrapSelectorWheel(false);
        repeatView.setValue(DEFAULT_REPEAT);

        timerTextView = (TextView) config.findViewById(R.id.textView);

        Button addConfig = (Button) config.findViewById(R.id.addConfig);
        addConfig.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                addConfig(configContainer.indexOfChild(config) + 1);
            }
        });

        Button removeConfig = (Button) config.findViewById(R.id.removeConfig);
        removeConfig.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                configContainer.removeView(config);
            }
        });

        tempoView.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                stopTimer();
            }
        });

        beatsView.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                stopTimer();

                fillContainer(config);
            }
        });

        repeatView.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                stopTimer();
            }

        });

        initVars();
        fillContainer(config);
    }

    private void resetTimer() {
        counterOfConfig = 0;
        initVars();
        resetAccents();
        toggleMetro(true);
    }

    int[] arrayLengthOfConfigTicks;
    private void resetAccents() {
        arrayLengthOfConfigTicks = new int[configContainer.getChildCount()];

        int lengthOfTicks = 0;
        for (int i = 0; i < configContainer.getChildCount(); i++) {
            View config = configContainer.getChildAt(i);
            RecyclerView accentContainer = (RecyclerView) config.findViewById(R.id.my_recycler_view);
            int repeats = ((NumberPicker) config.findViewById(R.id.repeatsPicker)).getValue();
            lengthOfTicks += accentContainer.getAdapter().getItemCount() * repeats;

            arrayLengthOfConfigTicks[i] = accentContainer.getAdapter().getItemCount();
        }

        accents = new boolean[lengthOfTicks];
        accentsDataProvider = new ArrayList<ItemVO>();
        int cnt = 0;
        for (int i = 0; i < configContainer.getChildCount(); i++) {
            View config = configContainer.getChildAt(i);
            RecyclerView accentContainer = (RecyclerView) config.findViewById(R.id.my_recycler_view);
            AccentCheckBoxAdapter adapter = (AccentCheckBoxAdapter) accentContainer.getAdapter();
            accentsDataProvider.addAll(adapter.getDataSet());

            int repeats = ((NumberPicker) config.findViewById(R.id.repeatsPicker)).getValue();
            for (int k = 0; k < repeats; k++) {
                for (int j = 0; j < accentContainer.getAdapter().getItemCount(); j++) {
                    accents[cnt] = adapter.getDataSet().get(j).isChecked();
                    cnt++;
                }
            }
        }

    }

    private void stopTimer() {
        toggleMetro(false);

        counterOfConfig = 0;
        initVars();
        metroTask.getMetronome().setPlayBtnLabel("start");
    }
    private Thread uiThread;
    public void toggleMetro(boolean start) {
        if (start) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                metroTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
            else
                metroTask.execute();

            startUiThread();

        } else {
            metroTask.stop();
//            metroTask = new MetronomeAsyncTask();
        }
    }

    private int repeatsCounter = 1;
    private void startUiThread(){
        final Runnable runn1 = new Runnable() {
            long time = System.currentTimeMillis();
            int currentBeat = 1;
            @Override
            public void run() {

                log.info(String.valueOf(currentBeat) + " = " + String.valueOf(counterOfConfig) + " = " + String.valueOf(repeats) + " = "+ repeatsCounter + "  !1-------------------------");

                time = System.currentTimeMillis();

                metroTask.getMetronome().setCurrentBeatUI(currentBeat);

                int startPoint = 0;
                if(counterOfConfig > 0){
                    for (int i = 1; i<= counterOfConfig ; i++){
                        startPoint += arrayLengthOfConfigTicks[counterOfConfig-1];
                    }
                }

                for (ItemVO item : accentsDataProvider) {
                    if (accentsDataProvider.get((currentBeat -1) + startPoint) != item) {
                        item.setCurrentAccent(false);
                    } else {
                        item.setCurrentAccent(true);
                    }
                }

                if (currentBeat == beats) {
                    currentBeat = 0;
                    log.info(String.valueOf(currentBeat) + " = " + String.valueOf(beats) + " = " + String.valueOf(repeats) + "  !3-------------------------");
                    repeatsCounter++;
                }


//                log.info(String.valueOf(System.currentTimeMillis() - time) + "  1-------------------------");


                currentBeat++;

            }
        };

        uiThread = new Thread(new Runnable() {
            public void run() {
                try {



                    log.info("start uiThread = "+repeats+" = "+ beats);
                    int ticks = repeats * beats;
                    for(int i = 0; i < ticks;i++){
                        log.info("running uiThread = " + i);
                        runOnUiThread(runn1);
                        TimeUnit.MILLISECONDS.sleep(MINUTE/ tempo);
                    }
                    log.info("finished uiThread = "+repeats+" = "+ beats);


                    if (counterOfConfig >= configContainer.getChildCount() - 1) { // the end
                        stopTimer();
                        metroTask.getMetronome().setCurrentBeatUI(1);
//                        uiThread.interrupt();
                        log.info("  STOP-------------------------");
                        return;
                    } else { // next
                        counterOfConfig++;
                        initVars();
//                        startUiThread();
//                        uiThread.interrupt();
                        uiThread.run();
                    }


                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        uiThread.start();
    }

    private void fillContainer(View config) {
        int beat = ((NumberPicker) config.findViewById(R.id.beatsPicker)).getValue();

        RecyclerView mRecyclerView = (RecyclerView) config.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        ((LinearLayoutManager) mLayoutManager).setOrientation(LinearLayout.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        mRecyclerView.removeAllViews();

        List<ItemVO> tempAccents = new ArrayList<ItemVO>(beat);
        AccentCheckBoxAdapter mAdapter = new AccentCheckBoxAdapter();

        for (int i = 0; i < beat; i++) {
            ItemVO item = new ItemVO();
            if (i == 0)
                item.setChecked(true);

            tempAccents.add(item);
        }

        mAdapter.setDataSet(tempAccents);

        mRecyclerView.setAdapter(mAdapter);
    }

    private void initVars() {
        View config = configContainer.getChildAt(counterOfConfig);
        tempo = ((NumberPicker) config.findViewById(R.id.tempoPicker)).getValue();
        beats = ((NumberPicker) config.findViewById(R.id.beatsPicker)).getValue();
        repeats = ((NumberPicker) config.findViewById(R.id.repeatsPicker)).getValue();

        metroTask.setBeat((short) beats);
        metroTask.setBpm((short) tempo);
    }


    LayoutInflater layoutInflater;
    int beats = DEFAULT_BEATS;
    int tempo = DEFAULT_TEMPO;
    int repeats = DEFAULT_REPEAT;
    boolean[] accents;
    List<ItemVO> accentsDataProvider;
    LinearLayout configContainer;
    TextView timerTextView;
    int counterOfConfig = 0;

    @Override
    public void onPause() {
        super.onPause();
        toggleMetro(false);
        Button b = (Button) findViewById(R.id.button);
        b.setText("start");
    }

    public void onBackPressed() {
        toggleMetro(false);
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
        finish();
    }

    private class MetronomeAsyncTask extends AsyncTask<Void, Void, String> {
        private Metronome metronome;

        MetronomeAsyncTask() {
            metronome = new Metronome();
            mainBinding.setMetronome(metronome);
        }

        protected String doInBackground(Void... params) {
            start();

            return null;
        }

        public void start() {
            metronome.setBeat(beats);
            metronome.setBpm(tempo);

            metronome.setAccents(accents);
            metronome.setAccentDataProvider(accentsDataProvider);

            metronome.play();
        }
        public void stop() {
            metronome.stop();
        }

        public void setBpm(short bpm) {
            metronome.setBpm(bpm);
            metronome.calcSilence();
        }

        public void setBeat(short beat) {
            if (metronome != null)
                metronome.setBeat(beat);
        }

        public void setAccents(boolean[] accents) {
            if (metronome != null)
                metronome.setAccents(accents);
        }

        public Metronome getMetronome() {
            return metronome;
        }
    }


    public class AccentCheckBoxAdapter extends RecyclerView.Adapter<AccentCheckBoxAdapter.AccentCheckBoxViewHolder> {
        private List<ItemVO> mDataset;

        public void setDataSet(List<ItemVO> dataSet) {
            mDataset = dataSet;
        }

        public List<ItemVO> getDataSet() {
            return mDataset;
        }

        @Override
        public AccentCheckBoxViewHolder onCreateViewHolder(final ViewGroup parent,
                                                                   int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            ItemBinding binding = ItemBinding.inflate(inflater, parent, false);

            return new AccentCheckBoxViewHolder(binding.getRoot());
        }

        @Override
        public void onBindViewHolder(AccentCheckBoxViewHolder holder, int position) {
            ItemVO item = (ItemVO) mDataset.get(position);

            holder.binding.setItem(item);
            holder.binding.setClicker(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    if (views != null && views.indexOf(v) != -1) {
//                        ItemVO item = mDataset.get(views.indexOf(v));
//                        item.setChecked(isChecked);
//                    }
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }

        public class AccentCheckBoxViewHolder extends RecyclerView.ViewHolder {

            ItemBinding binding;

            public AccentCheckBoxViewHolder(View itemView) {
                super(itemView);
                binding = DataBindingUtil.bind(itemView);
            }
        }
    }

}
