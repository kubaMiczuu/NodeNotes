import type {TaskData} from "../types/task.ts"

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

const NoteTaskCard = ({task, totalTasks}:TaskCardProps) => {

    const config = statusConfig[task.status];

    const creationDate = new Date(task.createdAt + "Z").toLocaleDateString();
    const updateDate = new Date(task.updatedAt + "Z").toLocaleDateString();

    const clampClass = totalTasks > 3 ? "line-clamp-4" : "line-clamp-16";

    return (
        <article className={`flex flex-col h-40 2xl:h-48 rounded-xl border-2 p-3 shadow-lg hover:scale-105 transition cursor-pointer overflow-hidden relative ${config.border}`}>

            <div className="flex justify-between items-baseline w-full">

                <h2 className={`text-md font-bold tracking-wide ${config.text}`}>
                    {config.label}
                </h2>

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

            <p className={`mt-4 ${clampClass} whitespace-pre-wrap`}>
                {task.description}
            </p>

            <div className={`absolute h-20 bottom-0 left-0 w-full bg-linear-to-t from-white to-transparent pointer-events-none`} />
        </article>
    )
}
export default NoteTaskCard