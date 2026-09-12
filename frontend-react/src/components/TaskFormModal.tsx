import {useState} from "react";
import {taskSchema} from "../schemas/taskSchema.ts";
import type {TaskFormData} from "../schemas/taskSchema.ts";
import {useForm, useWatch} from "react-hook-form";
import {zodResolver} from "@hookform/resolvers/zod";
import Modal from "./common/Modal.tsx";
import InputField from "./common/InputField.tsx";
import TextAreaField from "./common/TextAreaField.tsx";
import DeleteConfirmOverlay from "./common/DeleteConfirmOverlay.tsx";
import TaskStatusChangeButton from "./TaskStatusChangeButton.tsx";
import ModalFooter from "./common/ModalFooter.tsx";
import {axiosClient} from "../api/axiosClient.ts";
import TaskTypeSelectButton from "./TaskTypeSelectButton.tsx";
import type {TaskData} from "../types/task.ts"
import InteractiveTreeContainer from "./InteractiveTreeContainer.tsx";

interface TaskFormModalProps {
    mode: "UPDATE" | "ADD";
    initialData?: TaskData | null;
    onCancel: () => void;
    onSuccess: () => void;
}
const modeConfig = {
    ADD: {
        headerText: "Create a new task",
        buttonText: "Create task",
        titlePlaceholder: '',
        descriptionPlaceholderValue: '',
    },
    UPDATE: {
        headerText: "Update this task",
        buttonText: "Update task",
        titlePlaceholderValue: '',
        descriptionPlaceholderValue: ''
    }
}

const TaskFormModal = ({mode, initialData, onCancel, onSuccess}:TaskFormModalProps) => {

    const config = modeConfig[mode];

    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

    const {control, register, handleSubmit, setValue, formState: {errors}} = useForm<TaskFormData>({
        resolver: zodResolver(taskSchema),
        mode: "onTouched",
        defaultValues: {
            title: initialData?.title || "",
            description: initialData?.description || "",
            status: initialData?.status || "TODO",
            type: initialData?.type || "NOTE"
        }
    });

    const currentStatus = useWatch({
        control,
        name: 'status'
    })

    const currentType = useWatch({
        control,
        name: 'type'
    })

    const onSubmit = async (data: TaskFormData) => {
        if(mode === "ADD") {
            await axiosClient.post("/tasks", {title: data.title, description: data.description, type: data.type})
        } else if(mode === "UPDATE") {
            await axiosClient.put("/tasks/"+initialData?.id, {title: data.title, description: data.description, status: data.status})
        }

        onSuccess();
        onCancel();
    }

    const handleDeleteTask = async () => {
        await axiosClient.delete("/tasks/"+initialData?.id);
        onSuccess();
        onCancel();
    }

    return (
        <Modal onCancel={() => onCancel()}>

            <form onSubmit={handleSubmit(onSubmit)} onClick={(e) => e.stopPropagation()} className="flex flex-col w-full min-h-[calc(100vh-128px)]`">

                <h1 className={`text-center text-3xl font-bold text-slate-800`}>
                    {config.headerText}
                </h1>

                <div className={`flex flex-col gap-4 mt-4`}>

                    <InputField id={'title'} label={'Title'} placeholder={'Enter title'} register={register('title')} error={errors.title?.message}/>

                    {currentType === "NOTE" && (
                        <TextAreaField id={'description'} label={'Description'} placeholder={'Type something about this task...'} register={register('description')} error={errors.description?.message}/>
                    )}

                    {mode === "ADD" && (
                        <>
                            <h2 className={`text-md text-slate-700 font-bold`}>
                                Select Task Type
                            </h2>

                            <div className={`flex gap-4 justify-center`}>

                                <TaskTypeSelectButton type={"NOTE"} onValueChange={setValue} isActive={currentType === "NOTE"} />

                                <TaskTypeSelectButton type={"TREE"} onValueChange={setValue} isActive={currentType === "TREE"} />

                            </div>
                        </>
                    )}

                    {mode === "UPDATE" && currentType === "NOTE" && (
                        <div className={`flex gap-4 justify-center`}>

                            {currentStatus !== "TODO" && (
                                <TaskStatusChangeButton onValueChange={setValue} status={"TODO"} />
                            )}

                            {currentStatus !== "IN_PROGRESS" && (
                                <TaskStatusChangeButton onValueChange={setValue} status={"IN_PROGRESS"} />
                            )}

                            {currentStatus !== "DONE" && (
                                <TaskStatusChangeButton onValueChange={setValue} status={"DONE"} />
                            )}

                        </div>
                    )}

                    {mode === "UPDATE" && currentType === "TREE" && (
                        <InteractiveTreeContainer taskId={initialData?.id} initialItems={initialData?.children} />
                    )}

                </div>

                <div className={`flex flex-col-reverse md:flex-row items-center gap-4  mt-4 md:mt-16 w-full ${mode === "UPDATE" ? "justify-between" : "justify-center"}`}>

                    {mode === "UPDATE" && (
                        <button className="text-xl w-full md:w-auto px-6 py-2 text-rose-600 font-bold bg-rose-50 hover:bg-rose-100 hover:scale-105 rounded-lg transition cursor-pointer"
                                onClick={() => setShowDeleteConfirm(true)} type={'button'}
                        >
                            Delete task
                        </button>
                    )}

                    <ModalFooter onCancel={onCancel} submitText={config.buttonText} />

                    {showDeleteConfirm && (
                        <div className={`absolute inset-0 z-10 bg-white/90 backdrop-blur-sm flex items-center justify-center rounded-2xl`}>
                            <DeleteConfirmOverlay onCancel={setShowDeleteConfirm} onConfirm={handleDeleteTask} toDelete={"task"}/>
                        </div>
                    )}
                </div>

            </form>

        </Modal>
    )
}
export default TaskFormModal