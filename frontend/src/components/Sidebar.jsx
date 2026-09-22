import { NavLink } from "react-router-dom"

function Sidebar() {
  const links = [
    { name: "Dashboard", path: "/" },
    { name: "Investigations", path: "/investigations" },
    { name: "Network Analysis", path: "/network" },
    { name: "Evidence", path: "/evidence" },
    { name: "AI Intelligence", path: "/intelligence" },
  ]

  return (
    <aside className="w-64 min-h-screen bg-slate-950 border-r border-slate-800 p-5">

      <div className="mb-8">
        <h1 className="text-2xl font-bold text-cyan-400">
          CRIMENET AI
        </h1>
        <p className="text-xs text-slate-500 mt-1">
          Intelligence Platform
        </p>
      </div>

      <nav className="space-y-2">
        {links.map((link) => (
          <NavLink
            key={link.path}
            to={link.path}
            className={({ isActive }) =>
              `block px-4 py-3 rounded-lg transition ${
                isActive
                  ? "bg-cyan-500/10 text-cyan-400 border border-cyan-500/20"
                  : "text-slate-400 hover:bg-slate-800 hover:text-white"
              }`
            }
          >
            {link.name}
          </NavLink>
        ))}
      </nav>

    </aside>
  )
}

export default Sidebar