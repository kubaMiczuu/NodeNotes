import NoteTaskCard from "../components/NoteTaskCard.tsx"
import {useEffect, useState} from "react"
import TaskFormModal from "../components/TaskFormModal.tsx";
import type {TaskData} from "../types/task.ts"
import DashboardToolbar from "../components/DashboardToolbar.tsx";
import {axiosClient} from "../api/axiosClient.ts";
import Pagination from "../components/Pagination.tsx";
import {useDebounce} from "../hooks/useDebounce.ts";
import TreeTaskCard from "../components/TreeTaskCard.tsx";

const Dashboard = () => {

    const [modalMode, setModalMode] = useState<null | "ADD" | "UPDATE">(null);
    const [isLoading, setIsLoading] = useState<boolean>(true);
    const [selectedTask, setSelectedTask] = useState<TaskData | null>(null);
    const [currentPage, setCurrentPage] = useState<number>(0);
    const [totalPages, setTotalPages] = useState<number>(0);
    const [refreshTrigger, setRefreshTrigger] = useState<number>(0);
    const [tasks, setTasks] = useState<TaskData[]>([]);
    const [searchQuery, setSearchQuery] = useState<string>("");
    const debouncedSearchQuery = useDebounce(searchQuery, 300) || "";
    const [filterStatus, setFilterStatus] = useState<"ALL" | "TODO" | "IN_PROGRESS" | "DONE">("ALL");

    const handleUpdateClick = (task: TaskData) => {
        setSelectedTask(task);
        setModalMode("UPDATE");
    }

    const handleAddClick = () => {
        setSelectedTask(null);
        setModalMode("ADD");
    }

    const handleCancelClick = () => {
        setSelectedTask(null);
        setModalMode(null);
        setRefreshTrigger(prev => prev + 1);
    }

    const handleSearchChange = (newQuery:string) => {
        setCurrentPage(0);
        setSearchQuery(newQuery);
    }

    const handleFilterChange = (newStatus:"ALL" | "TODO" | "IN_PROGRESS" | "DONE") => {
        setCurrentPage(0);
        setFilterStatus(newStatus);

    }

    useEffect(() => {
        const fetchData = async () => {
            setIsLoading(true);

            const requestParams: { page: number; size: number, status?: string, title?: string } = {
                page: currentPage,
                size: 6
            };

            if (debouncedSearchQuery !== "") {
                requestParams.title = debouncedSearchQuery;
            }
            if (filterStatus !== "ALL") {
                requestParams.status = filterStatus;
            }

            axiosClient.get("/tasks", {
                params: requestParams
            })
                .then((response) => {
                    setTasks(response.data.content);
                    setTotalPages(response.data.totalPages);
                })
                .finally(() => {
                    setIsLoading(false);
                });
        };

        void fetchData();

    }, [currentPage, refreshTrigger, debouncedSearchQuery, filterStatus]);

    const isSearchingOrFiltering = debouncedSearchQuery !== "" || filterStatus !== "ALL";

    const renderContent = () => {
        if (isLoading) {
            return (
                <div className="flex justify-center items-center h-full mt-20">
                    <p className="text-xl font-bold text-slate-500">Loading tasks...</p>
                </div>
            );
        }

        if(totalPages === 0) {
            if (isSearchingOrFiltering) {
                return (
                    <div className="flex flex-col items-center mt-20">
                        <h2 className="text-3xl font-bold text-slate-800">No tasks found!</h2>
                        <p className="text-slate-500 mt-2">Try adjusting your filters or search query.</p>
                        <button onClick={() => { setSearchQuery(""); setFilterStatus("ALL"); }}
                        className={'mt-4 bg-sky-400 text-white px-4 py-2 rounded-lg cursor-pointer hover:scale-105 transition font-bold'}>
                            Clear Filters
                        </button>
                    </div>
                );
            }
            return (
                <div className="flex flex-col items-center justify-center mt-10">
                    <h1 className={`text-5xl font-extrabold text-slate-800 mb-6 text-center`}>You do not have any tasks yet!</h1>
                    <h2 className={`text-lg max-w-2xl mx-auto font-bold text-slate-500 text-center`}>Lets create your very first task and discover how organised your life become!</h2>
                    <button onClick={() => {handleAddClick()}} className="mt-8 font-extrabold tracking-wider text-center w-full md:w-1/2 text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-4 py-3 rounded-xl cursor-pointer">
                        Click me to add your first task!
                    </button>
                </div>
            );
        }

        return (
            <>
                <ul className="flex-1 grid grid-cols-1 md:grid-cols-3 gap-6 p-4">
                    {tasks.map((task: TaskData) => (
                        <li key={task.id} className={`h-full block`} onClick={() => handleUpdateClick(task)}>
                            {task.type === "NOTE"
                                ? <NoteTaskCard task={task} totalTasks={tasks.length} />
                                : <TreeTaskCard task={task} totalTasks={tasks.length} />
                            }
                        </li>
                    ))}
                </ul>

                <div className="mt-3 p-4 flex items-center justify-center text-slate-400">
                    <Pagination currentPage={currentPage} setCurrentPage={setCurrentPage} totalPages={totalPages} />
                </div>
            </>
        );
    };

    return (
        <div className="flex justify-between px-4 cursor-default">
            <div className="flex flex-col w-full max-w-5xl min-h-[calc(100vh-128px)] bg-white border border-slate-100 shadow-sm shadow-slate-200/40 rounded-2xl p-6">
                
                <DashboardToolbar currentQuery={searchQuery} currentFilter={filterStatus} onAddClick={handleAddClick} onSearchChange={handleSearchChange} onFilterChange={handleFilterChange}/>

                {renderContent()}

            </div>

            {modalMode !== null && (
                <TaskFormModal mode={modalMode} initialData={selectedTask} onCancel={() => handleCancelClick()} onSuccess={() => setRefreshTrigger(prev => prev+1)} />
            )}
        </div>
    )
}

export default Dashboard;