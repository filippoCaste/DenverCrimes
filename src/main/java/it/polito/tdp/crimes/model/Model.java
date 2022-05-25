package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private Graph<String, DefaultWeightedEdge> grafo;
	private EventsDao dao;
	private List<String> best; //punto 2, il peso (da massimizzare è la size della List)
	
	public Model() {
		this.dao = new EventsDao();
	}
	
	public void creaGrafo(String categoria, int mese) { // funzione MONTH() nella query
		this.grafo = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		// aggiunta vertici
		Graphs.addAllVertices(grafo, dao.getVertici(mese, categoria));
		
		// aggiunta archi
		for(Adiacenza a : dao.getArchi(mese, categoria)) {
			// i vertici ci sono sicuramente perché la stessa query ma più puntuale
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(), a.getPeso());
		}
		
		System.out.println("Grafo creato con:\n - Archi: " + this.grafo.edgeSet().size() + ";\n - Vertici: "+this.grafo.vertexSet().size());
		
		// TODO riempimento delle tendine 2 e 3
	}
	
	public List<Adiacenza> getArchiMaggioriPesoMedio() {
		// scorrere archi del grafo per calcolare il peso medio --> oppure direttamente nella query
		double pesoTotMed = 0.0;
		
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoTotMed += this.grafo.getEdgeWeight(e);
		}
		
		pesoTotMed = pesoTotMed / this.grafo.edgeSet().size();
		System.out.println("\nPeso medio: " + pesoTotMed + "\n");
		
		// di nuovo si scorrono gli archi prendendo quelli maggiori della media
		List<Adiacenza> result = new ArrayList<Adiacenza>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e) > pesoTotMed) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e), this.grafo.getEdgeTarget(e), (int)(this.grafo.getEdgeWeight(e))));
			}
		}
		
		return result;
		
	}
	
	public List<String> calcolaPercorso(String sorgente, String destinazione) {
		this.best = new ArrayList<String>();
		
		List<String> parziale = new ArrayList<>();
		parziale.add(sorgente);
		
		this.cerca_ricorsivo(parziale, destinazione);
		
		return best;
	}

	private void cerca_ricorsivo(List<String> parziale, String destinazione) {
		// condizione di terminazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {
			// è la soluzione migliore
			if(this.best.size() < parziale.size()) {
				best = new ArrayList<>(parziale); // sovrascrittura con new!!
			}
			return; // arrivati a destinazione non ha senso proseguire
		}
		
		// scorrimento sui vicini dell'ultimo inserito e si esplorano le varie strade
		for(String vert : Graphs.neighborListOf(grafo, parziale.get(parziale.size()-1))) {
			
			// verifico che un vertice non appaia più volte --> cammino aciclico
			if(!parziale.contains(vert)) {
				parziale.add(vert);

				this.cerca_ricorsivo(parziale, destinazione);

				// backtracking
				parziale.remove(parziale.size()-1);
			}
		}
		
	}
	
	
}
