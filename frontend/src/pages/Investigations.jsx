import { useEffect, useState } from "react"

const rawApiBase = (typeof window !== "undefined" && window.__CRIMENET_BACKEND_URL__) || import.meta.env.VITE_BACKEND_URL || (typeof window !== "undefined" && localStorage.getItem("netra_backend_url")) || "http://localhost:8080";
const API_BASE = String(rawApiBase).trim().replace(/\/+$/, "");

function Investigations() {
  const [showForm, setShowForm] = useState(false)
  const [cases, setCases] = useState([])
  const [loading, setLoading] = useState(true)
  const [creating, setCreating] = useState(false)
  const [error, setError] = useState("")
  const [success, setSuccess] = useState("")

  const [form, setForm] = useState({
    caseId: "",
    title: "",
    investigationType: "Criminal Network",
    riskLevel: "HIGH",
    description: "",
    status: "ACTIVE",
  })

  const loadCases = async () => {
    try {
      setLoading(true)
      setError("")

      const response = await fetch(`${API_BASE}/api/cases`)

      if (!response.ok) {
        throw new Error(`Failed to load cases (${response.status})`)
      }

      const data = await response.json()
      setCases(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error("Case loading error:", err)
      setError("Cases load nahi ho paaye. Backend check karo.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadCases()
  }, [])

  const handleChange = (event) => {
    const { name, value } = event.target

    setForm((previous) => ({
      ...previous,
      [name]: value,
    }))
  }

  const handleCreateCase = async (event) => {
    event.preventDefault()

    setError("")
    setSuccess("")

    if (!form.caseId.trim()) {
      setError("Case ID required hai.")
      return
    }

    if (!form.title.trim()) {
      setError("Case Title required hai.")
      return
    }

    try {
      setCreating(true)

      /*
       * Backend Case model currently accepts:
       * caseId, title, riskLevel, entities, status
       *
       * investigationType and description are UI-side fields
       * and are therefore not sent to the current Case entity.
       */
      const payload = {
        caseId: form.caseId.trim(),
        title: form.title.trim(),
        riskLevel: form.riskLevel,
        entities: 0,
        status: form.status,
      }

      const response = await fetch(`${API_BASE}/api/cases`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      })

      const responseText = await response.text()

      if (!response.ok) {
        let message = `Failed to create case (${response.status})`

        try {
          const errorData = JSON.parse(responseText)

          if (errorData?.message) {
            message = errorData.message
          } else if (errorData?.error) {
            message = errorData.error
          }
        } catch {
          if (responseText) {
            message = responseText
          }
        }

        throw new Error(message)
      }

      setSuccess(`Case ${form.caseId} successfully created.`)

      setForm({
        caseId: "",
        title: "",
        investigationType: "Criminal Network",
        riskLevel: "HIGH",
        description: "",
        status: "ACTIVE",
      })

      setShowForm(false)

      await loadCases()
    } catch (err) {
      console.error("Case creation error:", err)
      setError(err.message || "Case create nahi ho paaya.")
    } finally {
      setCreating(false)
    }
  }

  const handleCancel = () => {
    setShowForm(false)
    setError("")
    setSuccess("")

    setForm({
      caseId: "",
      title: "",
      investigationType: "Criminal Network",
      riskLevel: "HIGH",
      description: "",
      status: "ACTIVE",
    })
  }

  const getRiskStyle = (risk) => {
    const value = String(risk || "").toUpperCase()

    if (value === "HIGH") {
      return "bg-red-500/10 text-red-400 border border-red-500/20"
    }

    if (value === "MEDIUM") {
      return "bg-yellow-500/10 text-yellow-400 border border-yellow-500/20"
    }

    return "bg-green-500/10 text-green-400 border border-green-500/20"
  }

  const getStatusStyle = (status) => {
    const value = String(status || "").toUpperCase()

    if (
      value === "ACTIVE" ||
      value === "INVESTIGATING" ||
      value === "OPEN"
    ) {
      return "bg-green-500/10 text-green-400 border border-green-500/20"
    }

    if (
      value === "CLOSED" ||
      value === "RESOLVED"
    ) {
      return "bg-slate-500/10 text-slate-400 border border-slate-500/20"
    }

    return "bg-yellow-500/10 text-yellow-400 border border-yellow-500/20"
  }

  return (
    <div className="space-y-8">

      {/* Header */}
      <div className="flex flex-col md:flex-row md:justify-between md:items-center gap-4">
        <div>
          <h2 className="text-3xl font-bold text-white">
            Investigations
          </h2>

          <p className="text-slate-400 mt-2">
            Create and manage criminal investigations
          </p>
        </div>

        <button
          onClick={() => {
            setShowForm((previous) => !previous)
            setError("")
            setSuccess("")
          }}
          className="px-5 py-3 bg-cyan-500 text-slate-950 rounded-lg font-semibold hover:bg-cyan-400 transition"
        >
          {showForm ? "Close Form" : "+ New Investigation"}
        </button>
      </div>

      {/* Success */}
      {success && (
        <div className="px-4 py-3 rounded-lg border border-green-500/20 bg-green-500/10 text-green-400">
          {success}
        </div>
      )}

      {/* Error */}
      {error && (
        <div className="px-4 py-3 rounded-lg border border-red-500/20 bg-red-500/10 text-red-400">
          {error}
        </div>
      )}

      {/* Create Investigation */}
      {showForm && (
        <form
          onSubmit={handleCreateCase}
          className="bg-slate-900 border border-slate-800 rounded-xl p-6"
        >
          <div className="flex items-center justify-between mb-6">
            <div>
              <h3 className="text-xl font-semibold text-white">
                Create New Investigation
              </h3>

              <p className="text-sm text-slate-500 mt-1">
                Register a new investigation in the intelligence platform
              </p>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">

            {/* Case ID */}
            <div>
              <label className="text-sm text-slate-400">
                Case ID *
              </label>

              <input
                name="caseId"
                value={form.caseId}
                onChange={handleChange}
                type="text"
                placeholder="e.g. CN-2026-001"
                className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
              />
            </div>

            {/* Case Title */}
            <div>
              <label className="text-sm text-slate-400">
                Case Title *
              </label>

              <input
                name="title"
                value={form.title}
                onChange={handleChange}
                type="text"
                placeholder="Enter case title"
                className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
              />
            </div>

            {/* Investigation Type */}
            <div>
              <label className="text-sm text-slate-400">
                Investigation Type
              </label>

              <select
                name="investigationType"
                value={form.investigationType}
                onChange={handleChange}
                className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
              >
                <option>Criminal Network</option>
                <option>Financial Crime</option>
                <option>Cyber Crime</option>
                <option>Organized Crime</option>
              </select>
            </div>

            {/* Risk */}
            <div>
              <label className="text-sm text-slate-400">
                Risk Level
              </label>

              <select
                name="riskLevel"
                value={form.riskLevel}
                onChange={handleChange}
                className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
              >
                <option value="HIGH">High</option>
                <option value="MEDIUM">Medium</option>
                <option value="LOW">Low</option>
              </select>
            </div>

            {/* Status */}
            <div>
              <label className="text-sm text-slate-400">
                Status
              </label>

              <select
                name="status"
                value={form.status}
                onChange={handleChange}
                className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
              >
                <option value="ACTIVE">Active</option>
                <option value="INVESTIGATING">Investigating</option>
                <option value="OPEN">Open</option>
                <option value="UNDER REVIEW">Under Review</option>
                <option value="CLOSED">Closed</option>
              </select>
            </div>

          </div>

          {/* Description */}
          <div className="mt-5">
            <label className="text-sm text-slate-400">
              Case Description
            </label>

            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows="4"
              placeholder="Describe the investigation..."
              className="w-full mt-2 p-3 bg-slate-950 text-white border border-slate-700 rounded-lg outline-none focus:border-cyan-400"
            />
          </div>

          {/* Actions */}
          <div className="flex gap-3 mt-6">

            <button
              type="submit"
              disabled={creating}
              className="px-6 py-3 bg-cyan-500 text-slate-950 rounded-lg font-semibold hover:bg-cyan-400 disabled:opacity-50 disabled:cursor-not-allowed transition"
            >
              {creating ? "Creating..." : "Create Case"}
            </button>

            <button
              type="button"
              onClick={handleCancel}
              disabled={creating}
              className="px-6 py-3 bg-slate-800 text-white rounded-lg hover:bg-slate-700 transition"
            >
              Cancel
            </button>

          </div>
        </form>
      )}

      {/* Cases */}
      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">

        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <div>
            <h3 className="text-xl font-semibold text-white">
              Active Investigations
            </h3>

            <p className="text-sm text-slate-500 mt-1">
              Cases registered in the investigation system
            </p>
          </div>

          <button
            onClick={loadCases}
            disabled={loading}
            className="px-4 py-2 bg-slate-800 text-slate-300 rounded-lg hover:bg-slate-700 disabled:opacity-50"
          >
            {loading ? "Loading..." : "Refresh"}
          </button>
        </div>

        <div className="overflow-x-auto">

          {loading ? (
            <div className="p-10 text-center text-slate-400">
              Loading cases...
            </div>
          ) : cases.length === 0 ? (
            <div className="p-10 text-center">
              <div className="text-slate-500 text-4xl mb-3">
                ◌
              </div>

              <p className="text-slate-300 font-medium">
                No investigations found
              </p>

              <p className="text-slate-500 text-sm mt-1">
                Create your first investigation using the button above.
              </p>
            </div>
          ) : (
            <table className="w-full text-left">

              <thead className="bg-slate-950 text-slate-400 text-sm">
                <tr>
                  <th className="px-6 py-4">Case ID</th>
                  <th className="px-6 py-4">Case</th>
                  <th className="px-6 py-4">Entities</th>
                  <th className="px-6 py-4">Risk</th>
                  <th className="px-6 py-4">Status</th>
                </tr>
              </thead>

              <tbody>
                {cases.map((caseItem) => (
                  <tr
                    key={caseItem.caseId}
                    className="border-t border-slate-800 hover:bg-slate-800/40 transition"
                  >

                    <td className="px-6 py-5 text-cyan-400 font-medium">
                      {caseItem.caseId}
                    </td>

                    <td className="px-6 py-5 text-white">
                      {caseItem.title || "Untitled Investigation"}
                    </td>

                    <td className="px-6 py-5 text-slate-400">
                      {caseItem.entities ?? 0}
                    </td>

                    <td className="px-6 py-5">
                      <span
                        className={`px-3 py-1 text-xs rounded-full ${getRiskStyle(
                          caseItem.riskLevel
                        )}`}
                      >
                        {caseItem.riskLevel || "LOW"}
                      </span>
                    </td>

                    <td className="px-6 py-5">
                      <span
                        className={`px-3 py-1 text-xs rounded-full ${getStatusStyle(
                          caseItem.status
                        )}`}
                      >
                        {caseItem.status || "OPEN"}
                      </span>
                    </td>

                  </tr>
                ))}
              </tbody>

            </table>
          )}

        </div>
      </div>

    </div>
  )
}

export default Investigations