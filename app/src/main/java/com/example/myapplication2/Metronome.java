package com.example.myapplication2;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import java.util.List;
import java.util.logging.Logger;

public class Metronome  extends BaseObservable {

	Logger log = Logger.getLogger(Metronome.class.getName());

	private double beatSound = 2440;
	private double sound = 6440;


	private double bpm;
	private int beat;
	private int silence;
	private final int tick = 1000; // samples of tick
	
	private boolean play = true;
	
	private AudioGenerator audioGenerator = new AudioGenerator(8000);
	private double[] soundTickArray;
	private double[] soundTockArray;
	private double[] silenceSoundArray;

	private int currentBeat = 1;
	private String playBtnLabel;
	private List<ItemVO> accentDataProvider;

	@Bindable
	public String getPlayBtnLabel(){
		return playBtnLabel;
	}

	public void setPlayBtnLabel(String playBtnLabel){
		this.playBtnLabel = playBtnLabel;
		notifyPropertyChanged(com.example.myapplication2.BR.playBtnLabel);
	}

	@Bindable
	public int getCurrentBeat(){
		return currentBeat;
	}

	public void setCurrentBeat(int currentBeat){
		this.currentBeat = currentBeat;
		notifyPropertyChanged(com.example.myapplication2.BR.currentBeat);
	}

	private int currentBeatUI = 1;

	@Bindable
	public int getCurrentBeatUI(){
		return currentBeatUI;
	}

	public void setCurrentBeatUI(int currentBeatUI){
		this.currentBeatUI = currentBeatUI;
		notifyPropertyChanged(com.example.myapplication2.BR.currentBeatUI);
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
	
	public void calcSilence() {
		silence = (int) (((60/bpm)*8000)-tick);		
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
	
	public void play() {
		calcSilence();
		do {

			if(accents[beatsCounter])
				audioGenerator.writeSound(soundTockArray);
			else
				audioGenerator.writeSound(soundTickArray);

			audioGenerator.writeSound(silenceSoundArray);

			setCurrentBeat(currentBeat + 1);
			if(currentBeat > beat)
				setCurrentBeat(1);

			beatsCounter++;
			if(beatsCounter > accents.length-1){
				beatsCounter = accents.length-1;
			}
		} while(play);
	}
	
	public void stop() {
		play = false;
		audioGenerator.destroyAudioTrack();
	}

	public double getBpm() {
		return bpm;
	}

	public void setBpm(int bpm) {
		this.bpm = bpm;
	}

	public int getBeat() {
		return beat;
	}

	public void setBeat(int beat) {
		this.beat = beat;
	}

	public void setAccents(boolean[] accents) {
		this.accents = accents;
	}

	public void setAccentDataProvider(List<ItemVO> accentDataProvider) {
		this.accentDataProvider = accentDataProvider;
	}

	public List<ItemVO> getAccentDataProvider() {
		return accentDataProvider;
	}
}
