import type {SubItemData} from "../types/task.ts"

interface TreeNodeProps {
    child?: SubItemData;
}

const TreeNode = ({child}:TreeNodeProps) => {
    return (
        <div className="flex flex-col whitespace-pre-wrap">

            <div className={`flex flex-row items-center ${child?.parentId == null ? "mt-4" : ""}`}>
                {child?.parentId && (
                    <div className="min-w-4 border-b-2 border-slate-300"></div>
                )}
                <p className={`pl-2 gap-1.5 truncate ${child?.isDone === true ? "line-through text-slate-400" : ""}`}>
                    {child?.text}
                </p>
            </div>

            {child?.children && child.children.length > 0 && (
                <div className="ml-6 flex flex-col">

                {child.children.map((subChild: SubItemData, index: number, arr: SubItemData[]) => {
                    const isLast = index === arr.length - 1;

                    return (
                        <div key={subChild.id} className="relative">

                            <div className={`absolute w-0.5 bg-slate-300 ${isLast ? 'h-3' : 'h-full'}`}></div>

                            <TreeNode child={subChild}/>
                        </div>
                    )
                })}
                </div>
            )}
        </div>
    )
}
export default TreeNode;