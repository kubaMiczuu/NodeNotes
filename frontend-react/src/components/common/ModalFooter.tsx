interface ModalFooterProps {
    onCancel: () => void;
    submitText: string;
}

const ModalFooter = ({onCancel, submitText}: ModalFooterProps) => {
    return (
        <div className="flex flex-col-reverse md:flex-row items-center mt-8 gap-4 w-full md:w-auto">

            <button onClick={() => onCancel()} type={'button'}
                    className="flex justify-center items-center gap-2 text-xl w-full md:w-auto px-6 py-2 text-slate-700 font-extrabold bg-slate-200 hover:bg-slate-300 hover:scale-105 rounded-lg transition cursor-pointer">

                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                     stroke="currentColor" className="w-5 h-5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="M6 18 18 6M6 6l12 12"/>
                </svg>

                Cancel

            </button>

            <button type="submit"
                    className="flex justify-center items-center gap-2 text-xl w-full md:w-auto px-8 py-2 text-white font-extrabold bg-sky-400 hover:bg-sky-500 hover:scale-105 rounded-lg transition cursor-pointer">

                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke-width="1.5"
                     stroke="currentColor" className="w-5 h-5">
                    <path stroke-linecap="round" stroke-linejoin="round" d="m4.5 12.75 6 6 9-13.5"/>
                </svg>

                {submitText}

            </button>

        </div>
    )
}

export default ModalFooter;