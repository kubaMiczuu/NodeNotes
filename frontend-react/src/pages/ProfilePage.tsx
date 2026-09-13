import ProfileInformation from "../components/ProfileInformation.tsx";
import ProfileStatCard from "../components/ProfileStatCard.tsx";
import {useContext, useEffect, useRef, useState} from "react";
import ProfileFormModal from "../components/ProfileFormModal.tsx";
import DeleteConfirmOverlay from "../components/common/DeleteConfirmOverlay.tsx";
import Modal from "../components/common/Modal.tsx";
import {axiosClient} from "../api/axiosClient.ts";
import {AuthContext} from "../context/AuthContext.tsx";
import {exportTasks} from "../utils/exportTasks.ts";

const ProfilePage = () => {

    const [modalMode, setModalMode] = useState<null | "EDIT" | "PASSWORD">(null);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [username, setUsername] = useState<string>("");

    const [overallTasks, setOverallTasks] = useState<number>(0);
    const [todoTasks, setTodoTasks] = useState<number>(0);
    const [inProgressTasks, setInProgressTasks] = useState<number>(0);
    const [doneTasks, setDoneTasks] = useState<number>(0);

    const [isImported, setIsImported] = useState<boolean>(false);

    const {checkSession} = useContext(AuthContext)

    const fileImportRef = useRef<HTMLInputElement>(null);

    const handleDeleteProfile = async () => {
        await axiosClient.delete("/users/me").then(() => {
            setShowDeleteConfirm(false);
        })

        await checkSession();
    }

    const handleExportAll = async () => {
        axiosClient.get("/tasks/export/all")
            .then((response) => {
                exportTasks(response.data, false);
            })
    }

    const handleImportAll = async (file:File) => {
        const reader = new FileReader();
        reader.onload = async (e) => {
            try {
                const fileContent = e.target?.result as string;
                const parsedData = JSON.parse(fileContent);

                await axiosClient.post("/tasks/import/all", parsedData);

                setIsImported(true);

            } catch (error) {
                console.error("Import error: ", error);
            }
        }

        reader.readAsText(file);
    }

    useEffect(() => {
        axiosClient.get("/users/me")
            .then(response =>{
                setUsername(response.data.username)
            });

        axiosClient.get("/tasks")
            .then((response) => {
                setOverallTasks(response.data.totalElements);
            })
        
        axiosClient.get("/tasks", {
            params: {
                status: "TODO"
            }
        })
            .then((response) => {
                setTodoTasks(response.data.totalElements);
            })

        axiosClient.get("/tasks", {
            params: {
                status: "IN_PROGRESS"
            }
        })
            .then((response) => {
                setInProgressTasks(response.data.totalElements);
            })

        axiosClient.get("/tasks", {
            params: {
                status: "DONE"
            }
        })
            .then((response) => {
                setDoneTasks(response.data.totalElements);
            })
    }, [isImported])

    return (

        <div className="flex justify-center px-4 cursor-default">

            <div className="flex flex-col w-full max-w-4xl min-h-[calc(100vh-128px)] bg-white border border-slate-100 shadow-sm shadow-slate-200/40 rounded-2xl p-6 gap-4">

                <ProfileInformation username={username} />

                <div className={`h-3/7 grid grid-cols-2 md:grid-cols-4 gap-6 p-4`}>

                    <ProfileStatCard status={"OVERALL"} value={overallTasks} />

                    <ProfileStatCard status={"TODO"} value={todoTasks} />

                    <ProfileStatCard status={"IN_PROGRESS"} value={inProgressTasks} />

                    <ProfileStatCard status={"DONE"} value={doneTasks} />

                </div>

                <div className={`flex gap-3 justify-around h-1/7`}>
                    <button type={"button"} onClick={() => fileImportRef.current?.click()}
                            className="text-xl w-full md:w-auto px-6 py-2 text-slate-800 font-bold bg-slate-200 hover:bg-slate-300 hover:scale-105 border border-slate-200 rounded-lg transition cursor-pointer"
                        >Import all task!
                    </button>
                    <input type="file" accept=".json" ref={fileImportRef} className="hidden" onChange={(e) => {
                        const file:File | undefined = e.target.files?.[0];
                        if (file) {
                            handleImportAll(file)
                        }
                        e.target.value = "";
                    }} />

                    <button type={"button"} onClick={() => handleExportAll()}
                            className="text-xl w-full md:w-auto px-6 py-2 text-slate-800 font-bold bg-slate-200 hover:bg-slate-300 hover:scale-105 border border-slate-200 rounded-lg transition cursor-pointer">
                        Export all task!
                    </button>

                </div>

                <div className={`h-2/7 grid grid-cols-1 md:grid-cols-3 gap-6 p-4 mt-16`}>

                    <button onClick={() => setModalMode("EDIT")} className="font-extrabold tracking-wider text-center w-full text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-4 py-3 rounded-xl cursor-pointer">
                        Edit profile
                    </button>

                    <button onClick={() => setModalMode("PASSWORD")} className="font-extrabold tracking-wider text-center w-full text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-4 py-3 rounded-xl cursor-pointer">
                        Change password
                    </button>

                    <button className="text-xl w-full md:w-auto px-6 py-2 text-rose-600 font-bold bg-rose-50 hover:bg-rose-100 hover:scale-105 rounded-lg transition cursor-pointer"
                            onClick={() => setShowDeleteConfirm(true)} type={'button'}
                    >
                        Delete profile
                    </button>


                </div>

            </div>

            {modalMode !== null && (
                <ProfileFormModal mode={modalMode} username={username} onCancel={() => setModalMode(null)} />
            )}

            {showDeleteConfirm && (
                <Modal onCancel={() => setShowDeleteConfirm(false)}>
                    <DeleteConfirmOverlay onCancel={() => setShowDeleteConfirm(false)} onConfirm={handleDeleteProfile} toDelete={"profile"}/>
                </Modal>
            )}

        </div>
    )
}

export default ProfilePage