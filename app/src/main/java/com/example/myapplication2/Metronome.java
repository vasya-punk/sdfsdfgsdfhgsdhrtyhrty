package com.example.myapplication2;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import java.util.logging.Logger;

public class Metronome  extends BaseObservable {

	Logger log = Logger.getLogger(Metronome.class.getName());

	private double beatSound = 2440;
	private double sound = 6440;


	private double tempo;
	private int beats;

	private int silence;
	private final int tick = 1000; // samples of tick
	
	private boolean play = true;
	private LinearLayout configContainer;
	private int repeats;

	public boolean isPlay(){
		return play;
	}
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;

	private String playBtnLabel = "Start";

	@Bindable
	public String getPlayBtnLabel(){
		return playBtnLabel;
	}

	public void setPlayBtnLabel(String playBtnLabel){
		this.playBtnLabel = playBtnLabel;
		notifyPropertyChanged(com.example.myapplication2.BR.playBtnLabel);
	}

	private int currentBeat = 1;

	@Bindable
	public int getCurrentBeat(){
		return currentBeat;
	}

	public void setCurrentBeat(int currentBeat){
		this.currentBeat = currentBeat;
		notifyPropertyChanged(com.example.myapplication2.BR.currentBeat);
	}

	private int beatsCounter = 0;

	@Bindable
	public int getBeatsCounter(){
		return beatsCounter;
	}

	public void setBeatsCounter(int beatsCounter){
		this.beatsCounter = beatsCounter;
		notifyPropertyChanged(com.example.myapplication2.BR.beatsCounter);
	}

	private boolean[] accents;

	public Metronome() {
		audioGenerator.createPlayer();
	}
	
	private void calcSilence() {
		silence = (int) (((60/ tempo)*8000)-tick);
		soundTickArray = new double[this.tick];
		soundTockArray = new double[this.tick];
		silenceSoundArray = new double[this.silence];
		double[] tick = audioGenerator.getSineWave(this.tick, 8000, beatSound);
		double[] tock = audioGenerator.getSineWave(this.tick, 8000, sound);
		for(int i=0;i<this.tick;i++) {
			soundTickArray[i] = tick[i];
			soundTockArray[i] = tock[i];
		}
		for(int i=0;i<silence;i++)
			silenceSoundArray[i] = 0;
	}

	private int counterOfConfig;
	private int counterOfRepeats;
	public void play() {
		initVars();
		calcSilence();
		counterOfRepeats = 0;

		for (int i = 0; i < accents.length; i++){
			if(!play) break;

			if(counterOfRepeats == beats * repeats){
				counterOfConfig++;
				initVars();
				calcSilence();
				counterOfRepeats = 0;
			}

//			log.info("!!!!!!!!!!! "+beatsCounter+ " = " + accents[beatsCounter]+ " = " + beats + " = " + repeats + " = " + counterOfConfig);
			if(accents[i])
				audioGenerator.writeSound(soundTockArray);
			else
				audioGenerator.writeSound(soundTickArray);

			audioGenerator.writeSound(silenceSoundArray);

			counterOfRepeats++;
		}
	}

	private void initVars() {
		View config = configContainer.getChildAt(counterOfConfig);
		tempo = ((NumberPicker) config.findViewById(R.id.tempoPicker)).getValue();
		beats = ((NumberPicker) config.findViewById(R.id.beatsPicker)).getValue();
		repeats = ((NumberPicker) config.findViewById(R.id.repeatsPicker)).getValue();
	}

	public void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	public void setAccents(boolean[] accents) {
		this.accents = accents;
	}

	public void setConfigContainer(LinearLayout configContainer) {
		this.configContainer = configContainer;
	}
}
