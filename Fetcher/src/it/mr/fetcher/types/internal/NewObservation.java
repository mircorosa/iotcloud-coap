package it.mr.fetcher.types.internal;

import it.mr.types.com.ResponseDescriptor;
import it.mr.fetcher.types.scenario.FetcherObservation;

/**
 * Created by mirco on 17/04/16.
 */
//TODO Rename this properly
public class NewObservation {
	private FetcherObservation observation;
	private ResponseDescriptor respDescriptor;

	public NewObservation(FetcherObservation observation, ResponseDescriptor respDescriptor) {
		this.observation = observation;
		this.respDescriptor = respDescriptor;
	}

	public FetcherObservation getObservation() {
		return observation;
	}

	public void setObservation(FetcherObservation observation) {
		this.observation = observation;
	}

	public ResponseDescriptor getRespDescriptor() {
		return respDescriptor;
	}

	public void setRespDescriptor(ResponseDescriptor respDescriptor) {
		this.respDescriptor = respDescriptor;
	}
}
