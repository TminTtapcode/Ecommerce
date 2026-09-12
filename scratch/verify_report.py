# -*- coding: utf-8 -*-
import sys

def main():
    sys.stdout.reconfigure(encoding='utf-8')
    with open('docs/REPORT_FULL_DETAIL.md', 'r', encoding='utf-8') as f:
        text = f.read()

    print('=== VERIFICATION OF REPORT_FULL_DETAIL.MD ===')
    print('Total characters:', len(text))
    print('Total lines:', len(text.splitlines()))

    checks = [
        ('FloatingChatWidget', True),
        ('Admin Dashboard', True),
        ('Vendor Analytics', True),
        ('22 bảng', True),
        ('UC09', True),
        ('UC10', True),
        ('UC11', True),
        ('BFS Category Traversal', True),
        ('Hybrid Recommendation Engine', True),
        ('restoreStockOnCancellation', True),
        ('Chat UI production chưa hoàn thiện', False),
        ('Admin Dashboard chỉ có text placeholder', False),
        ('ProfilePage chỉ hiển thị, không chỉnh sửa', False),
        ('Không có API thống kê doanh thu', False)
    ]

    all_passed = True
    for term, should_exist in checks:
        count = text.count(term)
        ok = (count > 0) if should_exist else (count == 0)
        status = 'PASS' if ok else 'FAIL'
        if not ok:
            all_passed = False
        print(f'[{status}] "{term}": found {count} times (Expected: {">0" if should_exist else "0"})')

    figure_count = text.count('[INSERT FIGURE:')
    diagram_count = text.count('[INSERT DIAGRAM:')
    print(f'Preserved figure placeholders: {figure_count}')
    print(f'Preserved diagram placeholders: {diagram_count}')
    print(f'OVERALL STATUS: {"ALL CHECKS PASSED!" if all_passed else "SOME CHECKS FAILED"}')

if __name__ == '__main__':
    main()
