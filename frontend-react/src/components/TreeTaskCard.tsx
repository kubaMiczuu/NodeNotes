import type {TaskData, SubItemData} from "../types/task.ts"
import TreeNode from "./TreeNode.tsx";

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

    return (
        <article className={`flex flex-col h-full rounded-xl border-2 p-3 shadow-lg hover:scale-105 transition cursor-pointer ${config.border}`}>

            <div className="flex justify-between w-full">

                <h2 className={`text-md font-bold tracking-wide ${config.text}`}>
                    Progression Bar
                </h2>

                <span className={`text-xs ${config.text} italic`}>
                    {creationDate !== updateDate
                        ? `Last modified: ${updateDate}`
                        : `Created: ${creationDate}`
                    }
                </span>

            </div>

            <h1 className={`text-xl text-center mt-2`}>
                {task.title}
            </h1>

            <div className={`max-h-32 ${totalTasks > 3 ? "md:max-h-32" : "md:max-h-84"} overflow-hidden relative`}>
                {task.children?.map((child:SubItemData) => (
                    <TreeNode child={child}/>
                ))}

                <div className={`h-12 absolute bottom-0 w-full bg-linear-to-t from-white to-transparent`} />
            </div>

        </article>
    )
}
export default TreeTaskCard