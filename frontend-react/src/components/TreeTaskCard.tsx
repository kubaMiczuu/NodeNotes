import type {TaskData, SubItemData} from "../types/task.ts"
import TreeNode from "./TreeNode.tsx";
import {calculateTreeTaskProgress} from "../utils/calculateTreeTaskProgress.ts";

interface TaskCardProps {
    task: TaskData;
    totalTasks: number;
}

const statusConfig = {
    TODO: {
        border: "border-slate-200 shadow-slate-200/40 hover:border-slate-300",
        text: "text-slate-400",
        label: "TODO"
    },
    IN_PROGRESS: {
        border: "border-blue-200 shadow-blue-200/40 hover:border-blue-300",
        text: "text-blue-400",
        label: "IN PROGRESS"
    },
    DONE: {
        border: "border-green-200 shadow-green-200/40 hover:border-green-300",
        text: "text-green-400",
        label: "DONE"
    }
};

const TreeTaskCard = ({task, totalTasks}:TaskCardProps) => {

    const config = statusConfig[task.status];

    const creationDate:string = `${task.createdAt?.substring(0, 10)}`
    const updateDate:string = `${task.updatedAt?.substring(0, 10)}`

    const taskCompletionProgress = calculateTreeTaskProgress(task.children);

    return (
        <article className={`flex flex-col h-full rounded-xl border-2 p-3 shadow-lg hover:scale-105 transition cursor-pointer overflow-hidden relative ${config.border}`}>

            <div className="flex justify-between w-full">

                <div className={`w-4/7 bg-slate-200 rounded-full h-4`}>
                    <div
                        className={` ${taskCompletionProgress<100 ? "bg-sky-400" : "bg-green-400"} rounded-full h-4 transition-all duration-500 ease-in-out`}
                        style={{ width: `${taskCompletionProgress}%` }}
                    ></div>
                </div>

                <span className={`text-xs ${config.text} italic`}>
                    {creationDate !== updateDate
                        ? `Modified: ${updateDate}`
                        : `Created: ${creationDate}`
                    }
                </span>

            </div>

            <h1 className={`text-xl text-center mt-2`}>
                {task.title}
            </h1>

            <div className={`max-h-32 ${totalTasks > 3 ? "md:max-h-32" : "md:max-h-84"} overflow-hidden`}>
                {task.children?.map((child:SubItemData) => (
                    <TreeNode child={child}/>
                ))}
            </div>

            <div className={`absolute h-20 bottom-0 left-0 w-full bg-linear-to-t from-white to-transparent pointer-events-none`} />
        </article>
    )
}
export default TreeTaskCard