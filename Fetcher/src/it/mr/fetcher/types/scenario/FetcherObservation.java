package it.mr.fetcher.types.scenario;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;

/**
 * Created by mirco on 06/04/16.
 */
public class FetcherObservation {
	private CoapObserveRelation relation;
	private CoapHandler handler;
	private byte[] token = new byte[8];

	public FetcherObservation() {/*Empty Constructor*/}

	public FetcherObservation(byte[] token, CoapHandler handler, CoapObserveRelation relation) {
		this.token=token;
		this.handler = handler;
		this.relation = relation;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public CoapObserveRelation getRelation() {
		return relation;
	}

	public void setRelation(CoapObserveRelation relation) {
		this.relation = relation;
	}

	public CoapHandler getHandler() {
		return handler;
	}

	public void setHandler(CoapHandler handler) {
		this.handler = handler;
	}
}
