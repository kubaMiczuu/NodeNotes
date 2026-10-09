import ProfileInformation from "../components/ProfileInformation.tsx";
import ProfileStatCard from "../components/ProfileStatCard.tsx";
import {useContext, useEffect, useRef, useState} from "react";
import ProfileFormModal from "../components/ProfileFormModal.tsx";
import DeleteConfirmOverlay from "../components/common/DeleteConfirmOverlay.tsx";
import Modal from "../components/common/Modal.tsx";
import {axiosClient} from "../api/axiosClient.ts";
import {AuthContext} from "../context/AuthContext.tsx";
import {exportTasks} from "../utils/exportTasks.ts";
import SectionOverlay from "../components/common/SectionOverlay.tsx";

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

                <SectionOverlay headerText={"Statistics"} headerDescription={"Track your productivity and task completion progress."}>

                    <div className={`grid grid-cols-2 md:grid-cols-4 gap-3`}>

                        <ProfileStatCard status={"OVERALL"} value={overallTasks}>
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M19.5 14.25v-2.625a3.375 3.375 0 0 0-3.375-3.375h-1.5A1.125 1.125 0 0 1 13.5 7.125v-1.5a3.375 3.375 0 0 0-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 0 0-9-9Z"/>
                            </svg>

                        </ProfileStatCard>

                        <ProfileStatCard status={"TODO"} value={todoTasks}>
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M8.25 6.75h12M8.25 12h12m-12 5.25h12M3.75 6.75h.007v.008H3.75V6.75Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0ZM3.75 12h.007v.008H3.75V12Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Zm-.375 5.25h.007v.008H3.75v-.008Zm.375 0a.375.375 0 1 1-.75 0 .375.375 0 0 1 .75 0Z"/>
                            </svg>

                        </ProfileStatCard>

                        <ProfileStatCard status={"IN_PROGRESS"} value={inProgressTasks}>
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M10.343 3.94c.09-.542.56-.94 1.11-.94h1.093c.55 0 1.02.398 1.11.94l.149.894c.07.424.384.764.78.93.398.164.855.142 1.205-.108l.737-.527a1.125 1.125 0 0 1 1.45.12l.773.774c.39.389.44 1.002.12 1.45l-.527.737c-.25.35-.272.806-.107 1.204.165.397.505.71.93.78l.893.15c.543.09.94.559.94 1.109v1.094c0 .55-.397 1.02-.94 1.11l-.894.149c-.424.07-.764.383-.929.78-.165.398-.143.854.107 1.204l.527.738c.32.447.269 1.06-.12 1.45l-.774.773a1.125 1.125 0 0 1-1.449.12l-.738-.527c-.35-.25-.806-.272-1.203-.107-.398.165-.71.505-.781.929l-.149.894c-.09.542-.56.94-1.11.94h-1.094c-.55 0-1.019-.398-1.11-.94l-.148-.894c-.071-.424-.384-.764-.781-.93-.398-.164-.854-.142-1.204.108l-.738.527c-.447.32-1.06.269-1.45-.12l-.773-.774a1.125 1.125 0 0 1-.12-1.45l.527-.737c.25-.35.272-.806.108-1.204-.165-.397-.506-.71-.93-.78l-.894-.15c-.542-.09-.94-.56-.94-1.109v-1.094c0-.55.398-1.02.94-1.11l.894-.149c.424-.07.765-.383.93-.78.165-.398.143-.854-.108-1.204l-.526-.738a1.125 1.125 0 0 1 .12-1.45l.773-.773a1.125 1.125 0 0 1 1.45-.12l.737.527c.35.25.807.272 1.204.107.397-.165.71-.505.78-.929l.15-.894Z"/>
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M15 12a3 3 0 1 1-6 0 3 3 0 0 1 6 0Z"/>
                            </svg>

                        </ProfileStatCard>

                        <ProfileStatCard status={"DONE"} value={doneTasks}>
                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M9 12.75 11.25 15 15 9.75M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z"/>
                            </svg>
                        </ProfileStatCard>

                    </div>

                </SectionOverlay>

                <SectionOverlay headerText={"Account Management"}
                                headerDescription={"Edit your profile or change password."}>

                    <div className={`flex flex-col md:flex-row gap-3`}>

                        <button onClick={() => setModalMode("EDIT")}
                                className="flex justify-center items-center gap-2 font-bold tracking-wider text-center md:w-auto w-full text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-6 py-3 rounded-lg cursor-pointer">

                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={1.5}
                                 stroke="currentColor" className="w-5 h-5">
                                <path strokeLinecap="round" strokeLinejoin="round" d="m16.862 4.487 1.687-1.688a1.875 1.875 0 1 1 2.652 2.652L6.832 19.82a4.5 4.5 0 0 1-1.897 1.13l-2.685.8.8-2.685a4.5 4.5 0 0 1 1.13-1.897L16.863 4.487Zm0 0L19.5 7.125" />
                            </svg>

                            Edit profile

                        </button>

                        <button onClick={() => setModalMode("PASSWORD")}
                                className="flex justify-center items-center gap-2 font-bold tracking-wider text-center md:w-auto w-full text-xl text-white bg-sky-400 hover:bg-sky-500 hover:scale-105 transition px-6 py-3 rounded-lg cursor-pointer">

                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M15.75 5.25a3 3 0 0 1 3 3m3 0a6 6 0 0 1-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1 1 21.75 8.25Z"/>
                            </svg>

                            Change password

                        </button>

                    </div>

                </SectionOverlay>

                <SectionOverlay headerText={"Data & Backup"}
                                headerDescription={"Export your tasks to a JSON file or restore them."}>

                    <div className={`flex flex-col md:flex-row gap-3`}>

                        <button type={"button"} onClick={() => fileImportRef.current?.click()}
                                className="flex justify-center items-center gap-2 text-xl w-full md:w-auto px-6 py-3 text-slate-800 font-bold bg-slate-200 hover:bg-slate-300 hover:scale-105 border border-slate-200 rounded-lg transition cursor-pointer">

                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5m-13.5-9L12 3m0 0 4.5 4.5M12 3v13.5"/>
                            </svg>

                            Import tasks

                        </button>

                        <input type="file" accept=".json" ref={fileImportRef} className="hidden" onChange={(e) => {
                            const file: File | undefined = e.target.files?.[0];
                            if (file) {
                                handleImportAll(file)
                            }
                            e.target.value = "";
                        }} />

                        <button type={"button"} onClick={() => handleExportAll()}
                                className="flex justify-center items-center gap-2 text-xl w-full md:w-auto px-6 py-3 text-slate-800 font-bold bg-slate-200 hover:bg-slate-300 hover:scale-105 border border-slate-200 rounded-lg transition cursor-pointer">

                            <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                                 stroke="currentColor" className="w-5 h-5">
                                <path stroke-linecap="round" stroke-linejoin="round"
                                      d="M3 16.5v2.25A2.25 2.25 0 0 0 5.25 21h13.5A2.25 2.25 0 0 0 21 18.75V16.5M16.5 12 12 16.5m0 0L7.5 12m4.5 4.5V3"/>
                            </svg>

                            Export tasks

                        </button>

                    </div>

                </SectionOverlay>

                <SectionOverlay headerText={"Danger Zone"}
                                headerDescription={"Permanently delete your account and all associated tasks."}
                                hideBorder={true}>

                    <button
                        className="flex justify-center items-center gap-2 text-xl w-full md:w-auto px-6 py-2 text-rose-600 font-bold bg-rose-50 hover:bg-rose-100 hover:scale-105 rounded-lg transition cursor-pointer"
                        onClick={() => setShowDeleteConfirm(true)} type={'button'}>

                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                             stroke="currentColor" className="w-5 h-5">
                            <path stroke-linecap="round" stroke-linejoin="round"
                                  d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0"/>
                        </svg>

                        Delete profile
                    </button>

                </SectionOverlay>

            </div>

            {modalMode !== null && (
                <ProfileFormModal mode={modalMode} username={username} onCancel={() => setModalMode(null)}/>
            )}

            {showDeleteConfirm && (
                <Modal onCancel={() => setShowDeleteConfirm(false)}>
                    <DeleteConfirmOverlay onCancel={() => setShowDeleteConfirm(false)} onConfirm={handleDeleteProfile}
                                          toDelete={"profile"}/>
                </Modal>
            )}

        </div>
    )
}

export default ProfilePage