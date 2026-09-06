interface TaskTypeSelectButtonProps {
    onValueChange: (text: "type", type: "NOTE" | "TREE") => void;
    type: "NOTE" | "TREE";
    isActive: boolean;
}

const stateConfig = {
    BASE: {
        button: "border-slate-200 shadow-slate-200/40 hover:border-slate-300",
        span: "text-slate-400"
    },
    SELECTED: {
        button: "border-sky-400 shadow-sky-200/40 hover:border-sky-300",
        span: "text-sky-400"
    }
}

const TaskTypeSelectButton = ({onValueChange, type, isActive}:TaskTypeSelectButtonProps) => {

   const activeConfig = isActive ? stateConfig["SELECTED"] : stateConfig["BASE"];

    return (
        <button onClick={() => {
            onValueChange("type", type)}

        }
                type={'button'}
                className={`w-full md:w-1/2 ${activeConfig.button} rounded-xl border-2 p-3 shadow-lg hover:scale-105 transition cursor-pointer`}
        >
            <span className={`italic ${activeConfig.span} font-bold tracking-wide`}> {type} TASK </span>
        </button>
    )
}
export default TaskTypeSelectButton;