import type {SubItemData} from "../types/task.ts";
import {axiosClient} from "../api/axiosClient.ts";
import {useEffect, useState} from "react";

interface TreeNodeProps {
    item: SubItemData
    onAddChild: (item: SubItemData, text:string) => void
    activeInputId: number | null,
    setActiveInputId: (id:number | null) => void,
    fetchTreeData: () => void,
    expandSignal: number,
    collapseSignal: number
}

const InteractiveTreeNode = ({item, onAddChild, activeInputId, setActiveInputId, fetchTreeData, expandSignal, collapseSignal}: TreeNodeProps) => {

    const isAddingChild = activeInputId === item.id;
    const [isExpanded, setExpanded] = useState<boolean>(!item.isDone);
    const [isEditing, setIsEditing] = useState<boolean>(false);


    const updateSubTaskStatus = async (itemToUpdate:SubItemData) => {
        await axiosClient.patch(`/subitems/${itemToUpdate?.id}/status?isDone=${itemToUpdate.isDone}`);
        fetchTreeData();
    }

    const updateSubTaskText = async (itemToUpdate:SubItemData, text:string) => {
        await axiosClient.patch(`/subitems/${itemToUpdate?.id}/text`, null, {
            params: { text: text }
        });
        fetchTreeData();
    }

    const deleteSubItem = async (itemToDelete:SubItemData) => {
        await axiosClient.delete(`/subitems/${itemToDelete?.id}`);
        fetchTreeData();
    }

    useEffect(() => {
        if(expandSignal == 0 &&  collapseSignal == 0) return;

        if(expandSignal > collapseSignal) {
            // eslint-disable-next-line react-hooks/set-state-in-effect
            setExpanded(true);
        } else {
            setExpanded(false);
        }

    }, [expandSignal, collapseSignal]);

    return (
        <>
            <div className={`flex flex-row gap-1 items-center ${item?.parentId == null ? "mt-4" : ""}`}>

                {item?.parentId && (
                    <div className="min-w-4 border-b-2 border-slate-300"></div>
                )}

                <input checked={item?.isDone || false} type={"checkbox"} onChange={() => updateSubTaskStatus(item)} />

                {item?.children && item.children.length > 0 && (
                    <button type="button" onClick={() => setExpanded(!isExpanded)}
                        className={`p-1 rounded-md transition-colors cursor-pointer text-slate-500 hover:text-slate-600`}
                    >
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor"
                            className={`w-4 h-4 transition-transform duration-200 ${isExpanded ? "rotate-90" : "rotate-0"}`}
                        >
                            <path strokeLinecap="round" strokeLinejoin="round" d="m8.25 4.5 7.5 7.5-7.5 7.5" />
                        </svg>
                    </button>
                )}


                {isEditing ? (
                    <input type={"text"}
                           defaultValue={item?.text}
                           autoFocus={true}
                           onBlur={() => setIsEditing(false)}
                           onKeyDown={(e) => {
                               if(e.key === 'Enter') {
                                   e.preventDefault();
                                   updateSubTaskText(item, e.currentTarget.value)
                                   setIsEditing(false);
                               }
                           }}
                           className={`pl-2 w-full outline-none text-slate-800 bg-slate-50 rounded-sm focus:ring-1 focus:ring-sky-400`}
                    />
                ) : (
                    <p className={` pl-2 gap-1.5 truncate ${item?.isDone ? "line-through text-slate-400" : "hover:text-slate-800"} overflow-x-hidden hover:scale-105 hover:cursor-pointer`}
                        onClick={() => setIsEditing(true)}>
                        {item?.text}
                    </p>
                )}


                <button type={'button'} onClick={() => setActiveInputId(item.id)} className={`text-xs hover:scale-105 cursor-pointer"`}>
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                         stroke="currentColor" className="w-5 h-5 text-slate-500 cursor-pointer hover:text-slate-600">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/>
                    </svg>
                </button>

                <button type={'button'} onClick={() => deleteSubItem(item)} className={`text-xs hover:scale-105 cursor-pointer`}>
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                         stroke="currentColor" className="w-5 h-5 text-slate-500 hover:text-slate-600">
                        <path stroke-linecap="round" stroke-linejoin="round"
                              d="m14.74 9-.346 9m-4.788 0L9.26 9m9.968-3.21c.342.052.682.107 1.022.166m-1.022-.165L18.16 19.673a2.25 2.25 0 0 1-2.244 2.077H8.084a2.25 2.25 0 0 1-2.244-2.077L4.772 5.79m14.456 0a48.108 48.108 0 0 0-3.478-.397m-12 .562c.34-.059.68-.114 1.022-.165m0 0a48.11 48.11 0 0 1 3.478-.397m7.5 0v-.916c0-1.18-.91-2.164-2.09-2.201a51.964 51.964 0 0 0-3.32 0c-1.18.037-2.09 1.022-2.09 2.201v.916m7.5 0a48.667 48.667 0 0 0-7.5 0"/>
                    </svg>

                </button>

            </div>

            {item?.children && item.children.length > 0 && isExpanded && (
                <div className="ml-6 flex flex-col">

                    {item.children.map((subChild: SubItemData, index: number, arr: SubItemData[]) => {
                        const isLast = index === arr.length - 1;

                        return (
                            <div key={subChild.id} className="relative">

                                <div className={`absolute w-0.5 bg-slate-300 ${isLast ? 'h-4' : 'h-full'}`}></div>

                                <InteractiveTreeNode item={subChild} onAddChild={onAddChild} fetchTreeData={fetchTreeData} activeInputId={activeInputId} setActiveInputId={setActiveInputId} expandSignal={expandSignal} collapseSignal={collapseSignal} />
                            </div>
                        )
                    })}
                </div>
            )}

            {isAddingChild && (

                <div className={`flex items-center ml-6 gap-2`}>

                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                         stroke="currentColor" className="w-5 h-5 text-gray-500">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4.5v15m7.5-7.5h-15"/>
                    </svg>

                    <input type={"text"}
                           autoFocus={true}
                           onBlur={() => setActiveInputId(null)}
                           onKeyDown={(e) => {
                               if(e.key === 'Enter') {
                                   e.preventDefault();
                                   onAddChild(item, e.currentTarget.value);
                                   e.currentTarget.value = "";
                               }
                           }}
                           placeholder={`Add Sub Item`}
                           className={`outline-none text-slate-800`}
                    />

                </div>

            )}
        </>
    )
}

export default InteractiveTreeNode;