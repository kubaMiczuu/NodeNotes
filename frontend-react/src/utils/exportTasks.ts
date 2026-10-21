import type {TaskData} from "../types/task.ts";

export const exportTasks = (taskToExport:TaskData | null | undefined, singleTask:boolean) => {
    const jsonTask = JSON.stringify(taskToExport, null, 2);
    const blob = new Blob([jsonTask], { type: "application/json" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;

    const commonFileName = singleTask ? "task.json" : "tasks.json";
    link.download = taskToExport?.title ? `${taskToExport?.title.replace(/\s+/g, '_')}.json` : commonFileName;

    document.body.appendChild(link);
    link.click();

    URL.revokeObjectURL(url);
}