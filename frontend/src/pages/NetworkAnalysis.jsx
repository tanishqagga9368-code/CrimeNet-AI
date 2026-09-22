function NetworkAnalysis() {
  return (
    <div>
      <h2 className="text-3xl font-bold">Network Analysis</h2>
      <p className="text-slate-400 mt-2">
        Analyze relationships between suspects, organizations and entities.
      </p>

      <div className="mt-8 h-96 rounded-xl border border-slate-800 bg-slate-900 flex items-center justify-center">
        <div className="text-center">
          <div className="text-5xl mb-3">🕸️</div>
          <h3 className="text-xl font-semibold">Criminal Network Graph</h3>
          <p className="text-slate-500 mt-2">
            Neo4j + Cytoscape.js
          </p>
        </div>
      </div>
    </div>
  )
}

export default NetworkAnalysis