from __future__ import annotations

from dataclasses import dataclass


@dataclass
class FlowTask:
    task_id: str
    title: str
    area: str
    task_type: str
    description: str
    acceptance_criteria: str
    dependencies: str
    val_command: str
    assigned_agent: str
    refs: str

    def to_brief(self) -> str:
        return (
            f"[{self.task_id}] {self.title} | area={self.area} | type={self.task_type} "
            f"| deps={self.dependencies or '-'} | owner={self.assigned_agent or '-'}"
        )


def prompt_for_batch(tasks: list[FlowTask], heading: str) -> str:
    lines = [heading, "", "Tasks:"]
    if not tasks:
        lines.append("- none")
        return "\n".join(lines)
    for task in tasks:
        lines.append(f"- {task.to_brief()}")
        if task.description:
            lines.append(f"  description: {task.description}")
        if task.acceptance_criteria:
            lines.append(f"  acceptance: {task.acceptance_criteria}")
        if task.refs:
            lines.append(f"  refs: {task.refs}")
        if task.val_command:
            lines.append(f"  val: {task.val_command}")
    return "\n".join(lines)
