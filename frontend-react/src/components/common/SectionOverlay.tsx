import type {ReactNode} from "react";

interface sectionOverlayProps {
    headerText: string;
    headerDescription: string;
    children: ReactNode;
    hideBorder?: boolean;
}

const SectionOverlay = ({headerText, headerDescription, children, hideBorder}:sectionOverlayProps) => {
    return (
        <section className={`flex flex-col gap-4 ${!hideBorder ? "border-b border-slate-200" : ""} py-4`}>
            <div>
                <h2 className="text-2xl font-bold text-slate-800">{headerText}</h2>
                <p className="text-slate-500 text-sm">{headerDescription}</p>
            </div>

            <div className="w-full mt-4">
                {children}
            </div>
        </section>
    )
}

export default SectionOverlay;