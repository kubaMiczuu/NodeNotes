import type {ReactNode} from "react";

interface profileStatCardProps  {
    status: "OVERALL" | "TODO" | "IN_PROGRESS" | "DONE";
    value: number;
    children: ReactNode;
}

const cardConfig = {
    OVERALL: {
        border: "border-slate-300 shadow-black-300/40 hover:border-black-400",
        middleBorder: "border-b border-slate-300",
        text: "text-black-400",
        label: "OVERALL",
        value: "text-black-500",
    },
    TODO: {
        border: "border-slate-200 shadow-slate-200/40 hover:border-slate-300",
        middleBorder: "border-b border-slate-200",
        text: "text-slate-400",
        label: "TO DO",
        value: "text-slate-500"
    },
    IN_PROGRESS: {
        border: "border-blue-200 shadow-blue-200/40 hover:border-blue-300",
        middleBorder: "border-b border-blue-200",
        text: "text-blue-400",
        label: "IN PROGRESS",
        value: "text-blue-500"
    },
    DONE: {
        border: "border-green-200 shadow-green-200/40 hover:border-green-300",
        middleBorder: "border-b border-green-200",
        text: "text-green-400",
        label: "DONE",
        value: "text-green-500"
    }
};

const ProfileStatCard = ({status, value, children}: profileStatCardProps) => {
    const config = cardConfig[status];

    return (
        <div className={`flex flex-col w-full border justify-center items-center rounded-xl gap-2 p-4 shadow-sm hover:shadow-md ${config.border}`}>

            <div className={`flex justify-center items-center gap-2 w-full text-center py-1 ${config.middleBorder} ${config.text}`}>
                {children}
                <h3 className={`text-sm md:text-md tracking-wide font-bold`}>{config.label}</h3>
            </div>

            <h4 className={`text-3xl md:text-4xl tracking-tight font-extrabold ${config.value}`}>{value}</h4>

        </div>
    )
}

export default ProfileStatCard