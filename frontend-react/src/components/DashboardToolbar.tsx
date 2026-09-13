interface DashboardToolbarProps {
    currentQuery: string;
    currentFilter: "ALL" | "TODO" | "IN_PROGRESS" | "DONE";
    currentType: "ALL" | "NOTE" | "TREE";
    onAddClick: () => void;
    onSearchChange: (newQuery:string) => void;
    onFilterChange: (newStatus:"ALL" | "TODO" | "IN_PROGRESS" | "DONE") => void;
    onTypeChange: (newType:"ALL" | "NOTE" | "TREE") => void;
}

const DashboardToolbar = ({currentQuery, currentFilter, currentType, onAddClick, onSearchChange, onFilterChange, onTypeChange}: DashboardToolbarProps) => {
    return (
        <div className={`flex flex-col p-4 pt-1`}>
            <div className="flex flex-col md:flex-row justify-center gap-4 p-3">

                <button onClick={() => onAddClick()} className="font-extrabold tracking-wider text-center w-full md:w-1/2 text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-4 py-3 rounded-xl cursor-pointer">
                    Click me to add new task!
                </button>

                <input value={currentQuery} onChange={(e) => {onSearchChange(e.target.value)}} placeholder={'Search for tasks...'} className={`border border-slate-200 rounded-lg p-2 w-full md:w-1/3 text-slate-500 focus:outline-none`}/>

                <select value={currentFilter} onChange={(e) => onFilterChange(e.target.value as "ALL" | "TODO" | "IN_PROGRESS" | "DONE")} className={`border border-slate-200 rounded-lg p-2 w-full md:w-1/3 text-slate-500 transition cursor-pointer focus:outline-none focus:ring-sky-300`}>

                    <option value="ALL">
                        All
                    </option>

                    <option value={'TODO'}>
                        TODO
                    </option>

                    <option value={'IN_PROGRESS'}>
                        IN PROGRESS
                    </option>

                    <option value={'DONE'}>
                        DONE
                    </option>
                </select>

            </div>

            <div className="flex flex-col md:flex-row justify-center gap-4 p-3">

                <button onClick={() => onTypeChange("ALL")}
                        className={`px-4 py-2 border border-slate-200 rounded-2xl text-sm transition font-semibold hover:scale-115 cursor-pointer ${currentType === "ALL" ? "bg-sky-400 font-bold text-white hover:bg-sky-500 active:bg-sky-500" : "bg-white text-slate-800"}`}
                >ALL TYPES</button>

                <button onClick={() => onTypeChange("NOTE")}
                        className={`px-4 py-2 border border-slate-200 rounded-2xl text-sm transition font-semibold hover:scale-115 cursor-pointer ${currentType === "NOTE" ? "bg-sky-400 font-bold text-white hover:bg-sky-500 active:bg-sky-500" : "bg-white text-slate-800"}`}
                >NOTES</button>

                <button onClick={() => onTypeChange("TREE")}
                        className={`px-4 py-2 border border-slate-200 rounded-2xl text-sm transition font-semibold hover:scale-115 cursor-pointer ${currentType === "TREE" ? "bg-sky-400 font-bold text-white hover:bg-sky-500 active:bg-sky-500" : "bg-white text-slate-800"}`}
                >TREES</button>

            </div>
        </div>
    )
}

export default DashboardToolbar;